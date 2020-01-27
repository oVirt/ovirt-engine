<%@ tag
    language="java"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    body-content="empty"
%>

<%--
    The PF4 Background image component includes a SVG that needed to be embedded in
    the page.  As per the docs, "This component puts an image on the background with an
    svg filter applied to it. The svg must be inline on the page for the filter to work
    in all browsers."

    https://www.patternfly.org/v4/documentation/core/components/backgroundimage
--%>
<div class="pf-c-background-image obrand_background-image">
  <svg xmlns="http://www.w3.org/2000/svg" class="pf-c-background-image__filter" width="0" height="0">
    <filter id="image_overlay">
      <feColorMatrix type="matrix" values="1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 0 0 0 1 0"></feColorMatrix>
      <feComponentTransfer color-interpolation-filters="sRGB" result="duotone">
        <feFuncR type="table" tableValues="0.086274509803922 0.43921568627451"></feFuncR>
        <feFuncG type="table" tableValues="0.086274509803922 0.43921568627451"></feFuncG>
        <feFuncB type="table" tableValues="0.086274509803922 0.43921568627451"></feFuncB>
        <feFuncA type="table" tableValues="0 1"></feFuncA>
      </feComponentTransfer>
    </filter>
  </svg>
</div>
