'''
Created on May 17, 2011

@author: Keith Robertson
'''
import unittest
import api


class Test(unittest.TestCase):


    def setUp(self):
        self.xmlDoc = api.parse("hosts.xml")   
        self.hostAry = self.xmlDoc.get_host()     
        pass


    def tearDown(self):
        pass


    def test_get_host_ary(self):
        self.assertEqual(len(self.hostAry), 2, "Error: there should be 2 hosts in hosts.xml.")
        pass
    
    def test_get_host_name(self):
        self.assertEqual(self.hostAry[0].get_name(), "rhevh22", 
                         "Error: first host should be rhev22.")
        self.assertEqual(self.hostAry[1].get_name(), "rhevh22-2", 
                         "Error: second host should be rhevh22-2.")        
        pass    

    def test_get_host_address(self):
        print "HERE"
        self.assertEqual(self.hostAry[0].get_address(), "192.168.122.20", 
                         "Error: first host IP should be 192.168.122.20.  Received (%s)" % 
                         (self.hostAry[0].get_address()))
        self.assertEqual(self.hostAry[1].get_address(), "192.168.122.21", 
                         "Error: second host IP should be 192.168.122.21.  Received (%s)" % 
                         (self.hostAry[1].get_address()))   
        pass

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()