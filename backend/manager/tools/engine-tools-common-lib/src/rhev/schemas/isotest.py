'''
Created on May 17, 2011

@author: Keith Robertson
'''
import unittest
import api


class Test(unittest.TestCase):


    def setUp(self):
        self.xmlDoc = api.parse("isodomain.xml")  
        self.domainAry = self.xmlDoc.get_storage_domain()     
        pass


    def tearDown(self):
        pass


    def test_get_domain_ary(self):
        self.assertEqual(len(self.domainAry), 1, "Error: there should be 1 domain in isodomain.xml.")
        pass
    
    def test_get_domain_name(self):
        self.assertEqual(self.domainAry[0].get_name(), "iso", 
                         "Error: first domain should be iso.")       
        pass    

    def test_get_domain_path(self):
        self.assertEqual(self.domainAry[0].get_storage().get_path(), "/virt/iso", 
                         "Error: path should be /virt/iso.  Received (%s)" % 
                         (self.domainAry[0].get_storage().get_path())) 
        pass
    
    def test_get_domain_type(self):
        self.assertEqual(self.domainAry[0].get_storage().get_type(), "nfs", 
                         "Error: type should be nfs.  Received (%s)" % 
                         (self.domainAry[0].get_storage().get_type())) 
        pass    
    
    def test_get_domain_id(self):
        self.assertEqual(self.domainAry[0].get_id(), "f349f3f3-ebf0-4c18-816f-c723a3ecf4ec", 
                         "Error: ID should be f349f3f3-ebf0-4c18-816f-c723a3ecf4ec.  Received (%s)" % 
                         (self.domainAry[0].get_id())) 
        pass        

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()