'''
Created on May 17, 2011
'''
import unittest
import ovfenvelope
from ovfenvelope import *
import pprint
import sys

class Test(unittest.TestCase):


    def setUp(self):
        self.xmlDoc = ovfenvelope.parse("sample-ovf.xml")
        self.section_ary = self.xmlDoc.get_Section()
        self.content_ary = self.xmlDoc.get_Content()
        self.ref_ary = self.xmlDoc.get_References()



        pass


    def tearDown(self):
        pass

    def test_rewrite(self):
        self.content_ary.get_Name().set_valueOf_("NEW-NAME-HERE")
        self.xmlDoc.export(sys.stdout,1)

    def test_get_file(self):
        def attr_finder(attr):
            if str(attr).endswith('href'):
                return attr
            else:
                return None
        file_ary = self.ref_ary.get_File()
        for file_type in file_ary:
            any_attrs = file_type.get_anyAttributes_()
            keys = any_attrs.keys()
            href_ary = filter(attr_finder, keys)
            pprint.pprint(href_ary)

        pass

    def test_get_section_ary(self):
        self.assertEqual(len(self.section_ary), 2, "Error: there should be 2 section elements in sample-ovf.xml.xml.")
        pass

    def test_get_DiskSection_Type(self):
        found = False
        for section in self.section_ary:
            if isinstance(section, DiskSection_Type):
                self.assertEqual(len(self.section_ary), 2, "Error: there should be 2 section elements in sample-ovf.xml.xml.")
                found = True
        if found:
            pass
        else:
            self.fail("No DiskSection_Type found.")

    def test_get_DiskSection_Type_Meta(self):
        def attr_finder(attr):
            if str(attr).endswith('diskId'):
                return attr
            else:
                return None

        found = False
        for section in self.section_ary:
            if isinstance(section, DiskSection_Type):
                disk_ary = section.get_Disk()
                if disk_ary is not None:
                    for disk in disk_ary:
                        any_attrs = disk.get_anyAttributes_()
                        if any_attrs is not None:
                            keys = any_attrs.keys()
                            diskID_ary = filter(attr_finder, keys)
                            if diskID_ary is not None:
                                pprint.pprint(diskID_ary)
                                found = True
                else:
                    print "Nothing in any"
        if found:
            pass
        else:
            self.fail("No DiskSection_Type found.")
        pass

    def test_content_ary(self):
        pprint.pprint(self.content_ary.get_Name().get_valueOf_())
        pass

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
