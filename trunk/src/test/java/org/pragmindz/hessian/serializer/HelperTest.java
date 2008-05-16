package org.pragmindz.hessian.serializer;

import org.pragmindz.hessian.serializer.helper.GenObjectHelper;
import org.pragmindz.hessian.serializer.namer.ChainNamer;
import org.pragmindz.hessian.serializer.namer.Namer;
import org.pragmindz.hessian.serializer.namer.IdentityNamer;
import org.pragmindz.hessian.model.HessianValue;
import junit.framework.TestCase;

public class HelperTest extends TestCase
{
    public static class Customer
    {
        private String name;
        private String firstName;
        private Address address;
    }

    public static class Address
    {
        private String street;
        private String city;
    }

    public static class ContactReference
    {
        private String name;
        private String street;
    }

    public static class CustomerHelper extends GenObjectHelper
    {

        protected CustomerHelper(final Class helpedclass, final String helpername, final String[] fieldNames, final Class[] fieldTypes)
        {
            super(helpedclass, helpername, fieldNames, fieldTypes);
        }

        protected Object fields2object(Object... aFields) throws Exception
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        protected void object2fields(Object aValue, Object... someFields) throws Exception
        {
            final Customer lCust = (Customer) aValue;
            someFields[0] = lCust.name + " " +  lCust.firstName;
            someFields[1] = lCust.address.street + " " + lCust.address.city;
        }
    }

    public static class CustomerNamer implements Namer
    {

        public String mapHessian2Java(String aHessianName) throws HessianSerializerException
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String mapJava2Hessian(String aJavaName) throws HessianSerializerException
        {
            if ("org.pragmindz.hessian.serializer.HelperTest$Customer".equals(aJavaName))
                return "ContactReference";
            return aJavaName;
        }
    }

    public void testHelper() throws HessianSerializerException
    {
        //Create our Customer
        final Customer lCust = new Customer();
        final Address lAddr = new Address();
        lAddr.street = "Bourbon str.";
        lAddr.city = "New York";
        lCust.name = "Lincoln";
        lCust.firstName = "Graham";
        lCust.address = lAddr;
        final HessianSerializer lSerializer = new HessianSerializer();
        lSerializer.getRepo().addHelper(new CustomerHelper(Customer.class, "Whatever", new String[] {"name","address"}, new Class[] {String.class, String.class}));
        lSerializer.setNamer(new ChainNamer(new Namer[] {new CustomerNamer(), new IdentityNamer()}));
        HessianValue lVal = lSerializer.serialize(lCust);
        System.out.println(lVal.prettyPrint());
    }
}
