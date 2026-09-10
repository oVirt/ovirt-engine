#!/usr/bin/env python3
"""Static consistency check for column/parameter types in the DB scripts.

This guards against *incomplete type changes* in the stored-procedure files under
``packaging/dbscripts``. This checks the stored procedures against the db schema to
ensure that all column types in the stored procedures are consistent with the schema.

The check is purely static (no database required) so it can run as an early,
fast CI gate.

Exit status: 0 when consistent, 1 when a mismatch is found.
"""

import argparse
import glob
import os
import re
import sys

# Map every PostgreSQL type spelling we understand (including multi-word ones)
# to a single canonical name. This is the ONE place to extend coverage: add a
# family's spellings here and the rest of the checker picks them up. Keys are
# lower-case; length/precision (e.g. VARCHAR(255)) is ignored.
SPELLING = {
    # integer family
    "smallint": "SMALLINT", "int2": "SMALLINT",
    "integer": "INTEGER", "int": "INTEGER", "int4": "INTEGER",
    "bigint": "BIGINT", "int8": "BIGINT",
    # boolean
    "boolean": "BOOLEAN", "bool": "BOOLEAN",
    # exact / floating-point numeric
    "numeric": "NUMERIC", "decimal": "NUMERIC",
    "real": "REAL", "float4": "REAL",
    "double precision": "DOUBLE PRECISION", "float8": "DOUBLE PRECISION",
    "float": "DOUBLE PRECISION",
    # character / text
    "character varying": "VARCHAR", "varchar": "VARCHAR",
    "character": "CHAR", "char": "CHAR", "bpchar": "CHAR",
    "text": "TEXT",
    # uuid
    "uuid": "UUID",
    # date / time
    "timestamp with time zone": "TIMESTAMPTZ", "timestamptz": "TIMESTAMPTZ",
    "timestamp without time zone": "TIMESTAMP", "timestamp": "TIMESTAMP",
    "date": "DATE", "time": "TIME",
    # binary / json
    "jsonb": "JSONB", "json": "JSON",
}
# Spellings tried longest-first so multi-word / longer names win over their
# prefixes (e.g. "timestamp with time zone" before "timestamp", "integer"
# before "int").
_SPELLINGS_BY_LEN = sorted(SPELLING, key=len, reverse=True)

# Width ordering *within the integer family only*, used to tell NARROWING
# (data-loss risk) from WIDENING (harmless). Canonical types not listed here
# have no ordering and are only ever compared for equality.
RANK = {"SMALLINT": 1, "INTEGER": 2, "BIGINT": 3}

# Character / serialized-text types are freely interchangeable in practice:
# VARCHAR/CHAR/TEXT are assignment-compatible, and JSON/JSONB columns are
# populated from VARCHAR/TEXT values by generic migration helpers. Differences
# *within* this family are therefore not real type-change bugs and are ignored.
TEXTUAL = {"VARCHAR", "CHAR", "TEXT", "JSON", "JSONB"}

CRED = '\033[91m'
CGREEN = '\033[92m'
CEND = '\033[0m'

CREATE_TABLE_RE = re.compile(r"\s*CREATE TABLE\s+([a-z0-9_]+)", re.IGNORECASE)
# A composite type definition (CREATE TYPE ... AS (...)). Its fields look like
# column declarations but are not table columns, so they must not be checked.
CREATE_TYPE_RE = re.compile(r"\s*CREATE\s+TYPE\b", re.IGNORECASE)
# A single column/parameter declaration on its own line: the name, then the type
# text up to an optional trailing comma or closing paren. The captured type text
# is fed to canon_type(), so non-declaration lines (canon_type -> None) are
# ignored. Lines ending in ';' (local DECLAREs, statements) never match.
DECL_RE = re.compile(
    r"^\s*(?:INOUT\s+|OUT\s+)?([a-z][a-z0-9_]*)\s+(.+?)\s*[,)]?\s*$",
    re.IGNORECASE,
)
# A column line inside CREATE TABLE: the name followed by the rest of the line
# (type plus any modifiers), which canon_type() then interprets.
COLUMN_RE = re.compile(r"\s*([a-z][a-z0-9_]*)\s+(.+)$")
QUOTED_RE = re.compile(r"'([^']*)'")
# Base tables referenced by a stored-procedure body. Views (``*_view``) simply
# never appear in the schema map, so they contribute no columns.
TABLE_REF_RE = re.compile(
    r"\b(?:INSERT\s+INTO|UPDATE|DELETE\s+FROM|FROM|JOIN)\s+([a-z][a-z0-9_]*)",
    re.IGNORECASE,
)


def script_dir():
    return os.path.dirname(os.path.abspath(__file__))


def dbscripts_dir():
    return os.path.normpath(os.path.join(script_dir(), "..", "packaging", "dbscripts"))


def canon_type(type_spec):
    """Canonical type name for a column/parameter type spec, or None when the
    type is outside the families the checker understands. Handles multi-word
    types and ignores length/precision and trailing modifiers, e.g.
    'character varying(255) NOT NULL' -> 'VARCHAR', 'BIGINT DEFAULT 0' -> 'BIGINT'."""
    s = re.sub(r"\([^)]*\)", " ", type_spec)   # drop (length) / (precision,scale)
    s = re.sub(r"[,;]", " ", s)                # detach trailing comma/semicolon
    s = " ".join(s.lower().split())
    for spelling in _SPELLINGS_BY_LEN:
        if s == spelling or s.startswith(spelling + " "):
            return SPELLING[spelling]
    return None


def _apply_column_type(schema, table, col, type_spec):
    """Record (or, when it becomes an unhandled type, drop) a column's type."""
    cols = schema.setdefault(table.lower(), {})
    canon = canon_type(type_spec)
    if canon is not None:
        cols[col.lower()] = canon
    else:
        cols.pop(col.lower(), None)


def build_schema(dbdir):
    """Return {table: {column: normalised_int_type}} reflecting the final schema:
    the CREATE TABLE definitions with the upgrade scripts' column additions and
    type changes applied on top, in filename order."""
    schema = {}

    current = None
    with open(os.path.join(dbdir, "create_tables.sql"), encoding="utf-8") as fh:
        for line in fh:
            m = CREATE_TABLE_RE.match(line)
            if m:
                current = m.group(1).lower()
                schema.setdefault(current, {})
                continue
            if current is None:
                continue
            if line.lstrip().startswith(")"):
                current = None
                continue
            cm = COLUMN_RE.match(line)
            if cm:
                canon = canon_type(cm.group(2))
                if canon is not None:
                    schema[current][cm.group(1).lower()] = canon

    # Apply upgrade scripts (numeric filename order) so retyped/added columns win.
    upgrades = sorted(glob.glob(os.path.join(dbdir, "upgrade", "**", "*.sql"),
                                recursive=True))
    for path in upgrades:
        with open(path, encoding="utf-8") as fh:
            for line in fh:
                low = line.lower()
                if "fn_db_add_column(" in low:
                    args = QUOTED_RE.findall(line)
                    if len(args) >= 3:  # (table, column, 'TYPE ...')
                        _apply_column_type(schema, args[0], args[1], args[2])
                if "fn_db_change_column_type(" in low:
                    args = QUOTED_RE.findall(line)
                    if len(args) >= 3:  # (table, column, [old,] 'NEW_TYPE')
                        _apply_column_type(schema, args[0], args[1], args[-1])
    return schema


def referenced_tables(text, fname, schema):
    """Base tables a stored-procedure file touches: those named in
    INSERT/UPDATE/DELETE/FROM/JOIN, plus the ``<name>_sp.sql`` filename stem."""
    tables = {m.group(1).lower() for m in TABLE_REF_RE.finditer(text)}
    if fname.endswith("_sp.sql"):
        tables.add(fname[:-len("_sp.sql")])
    return tables & set(schema)


def resolve_type(col, tables, schema):
    """The column's type as seen through the file's tables. Returns the single
    type when unambiguous, else None (unknown or conflicting -> not checkable)."""
    candidates = {schema[t][col] for t in tables if col in schema[t]}
    return next(iter(candidates)) if len(candidates) == 1 else None


def compatible(found, expected):
    """True when a declared type and the column type are effectively the same for
    the purposes of this check: identical, or both in the interchangeable textual
    family (VARCHAR/CHAR/TEXT/JSON/JSONB)."""
    if found == expected:
        return True
    return found in TEXTUAL and expected in TEXTUAL


def classify(found, expected):
    """Label the relationship between a declared type and the column type. Within
    the integer family we can distinguish NARROWING (data-loss risk) from
    WIDENING; across families (e.g. BOOLEAN vs INTEGER) it is a plain MISMATCH."""
    if found in RANK and expected in RANK:
        return "NARROWING" if RANK[found] < RANK[expected] else "WIDENING"
    return "MISMATCH"


def find_mismatches(dbdir, schema):
    """Scan every *_sp.sql file and return a list of mismatch dicts, resolving
    each column against the tables that file actually references."""
    mismatches = []
    for path in sorted(glob.glob(os.path.join(dbdir, "*.sql"))):
        fname = os.path.basename(path)
        if fname == "create_tables.sql":
            continue
        with open(path, encoding="utf-8") as fh:
            text = fh.read()
        tables = referenced_tables(text, fname, schema)
        in_type_block = False
        for lineno, line in enumerate(text.splitlines(), 1):
            # Skip fields of composite type definitions: they resemble column
            # declarations but are not table columns.
            if in_type_block:
                if ")" in line:
                    in_type_block = False
                continue
            if CREATE_TYPE_RE.match(line):
                if ")" not in line:   # multi-line CREATE TYPE ... AS ( ... )
                    in_type_block = True
                continue
            # Local DECLARE variables and assignments (lines ending in ';' or
            # containing ':=') are not signature parameters bound to a column,
            if line.rstrip().endswith(";") or ":=" in line:
                continue
            m = DECL_RE.match(line)
            if not m:
                continue
            name = m.group(1).lower()
            found = canon_type(m.group(2))
            if found is None:
                continue
            col = name[2:] if name.startswith("v_") else name
            expected = resolve_type(col, tables, schema)
            if expected is None or compatible(found, expected):
                continue
            owners = sorted(t for t in tables if col in schema[t])
            mismatches.append({
                "file": fname,
                "column": col,
                "found": found,
                "expected": expected,
                "line": lineno,
                "raw": line.strip(),
                "tag": classify(found, expected),
                "tables": owners,
            })
    return mismatches


def render(mismatches):
    for mm in mismatches:
        via = ", ".join(mm["tables"])
        print(CRED + "  {file}:{line}  {column}: found {found}, expected {expected} "
              "(from {via}, {tag})\n      {raw}".format(via=via, **mm) + CEND)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.parse_args(argv)

    dbdir = dbscripts_dir()
    schema = build_schema(dbdir)
    mismatches = find_mismatches(dbdir, schema)

    if mismatches:
        print(CRED + "FAIL: type mismatches found:\n" + CEND)
        render(mismatches)
        print()
        print(CRED + "db-type-consistency: {} mismatch(es)".format(
            len(mismatches)) + CEND)
        print(CRED + "\nFix the declarations above so their type matches the column "
              "present in the DB schema." + CEND)
        return 1
    print(CGREEN + "OK: no type mismatches." + CEND)
    return 0


if __name__ == "__main__":
    sys.exit(main())
