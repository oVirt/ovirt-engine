This package contains a generic python implementation of the oVirt Engine API (i.e. api.py). 
It was generated from the official oVirt Engine XML schema that is hosted at 
https://fedorahosted.org/rhevm-api/.

If the oVirt Engine API ever changes, the associated XML schema (i.e. api.xsd) should also change.
When this happens you may need to regenerate the python bindings for the oVirt Engine 
XML schema.  

To generate python bindings for the oVirt Engine XML schema do the following:

Step 0: Get generateDS from http://www.rexx.com/~dkuhlman/generateDS.html

Step 1: Regenerate the python bindings: "generateDS.py -o api.py api.xsd"

Step 2: The oVirt Engine XML schema does not define a single root element.  As such, 
        we need assist the generated python parser by creating a mapping of 
        elements to their generated python classes so that it can map the 
        input stream to a class. 
        
        What is a root element?  Well take a look at the XML returned by
        the RESTful function http://yourengineserver/api/hosts.  The
        XML returned by this function begins with <hosts> and <hosts> is 
        the root element.  Now look at http://yourengineserver/api/clusters.  
        The  XML returned by this function begins with <clusters> and <clusters> is 
        the root element.
        
        The process of creating the mapping is mildly tedious and 
        in the first first iteration of the API I erred on the side of caution because 
        I wasn't sure exactly which elements listed in api.xsd could actually be 
        root elements, as such, I mapped *every* element to an associated 
        python class.  This is probably overkill.
                
        Essentially what you need to do is to look at the schema (i.e. api.xsd) and 
        map every "<xs:element" to a class in api.py.  This task would be much easier
        if you knew the exact subset of elements that could actually be 
        root elements or if the XML schema were re-factored to a schema per RESTful 
        function. 
        
        Take a look at api.__rootClassMap for an example of the mapping.  
        
Step 3: I have added a 1 helper function in api.py and have commented out some 
        the code which echoes the parsed XML to stdout (this is annoying).  All
        of these modifications are denoted by a "NOT_GENERATED" comment
        and can be easily copied into a newly generated python bindings.
        
        
  
        