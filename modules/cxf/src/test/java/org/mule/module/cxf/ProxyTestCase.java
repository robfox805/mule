/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.cxf;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.module.cxf.testmodels.AsyncService;
import org.mule.tck.DynamicPortTestCase;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.transport.http.HttpConstants;
import org.mule.util.concurrent.Latch;

import java.util.HashMap;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class ProxyTestCase extends DynamicPortTestCase
{
    String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<soap:Body><test xmlns=\"http://foo\"> foo </test>" + "</soap:Body>" + "</soap:Envelope>";

    String doGoogleSearch = "<urn:doGoogleSearch xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:urn=\"urn:GoogleSearch\">";

    String msgWithComment = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<!-- comment 1 -->"
        + "<soap:Header>"
        + "<!-- comment 2 -->"
        + "</soap:Header>"
        + "<!-- comment 3 -->"
        + "<soap:Body>"
        + "<!-- comment 4 -->"
        + doGoogleSearch
        + "<!-- this comment breaks it -->"
        + "<key>1</key>"
        + "<!-- comment 5 -->"
        + "<q>a</q>"
        + "<start>0</start>"
        + "<maxResults>1</maxResults>"
        + "<filter>false</filter>"
        + "<restrict>a</restrict>"
        + "<safeSearch>true</safeSearch>"
        + "<lr>a</lr>"
        + "<ie>b</ie>"
        + "<oe>c</oe>"
        + "</urn:doGoogleSearch>"
        + "<!-- comment 6 -->"
        + "</soap:Body>"
        + "<!-- comment 7 -->"
        + "</soap:Envelope>";

    public void testServerWithEcho() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/Echo", msg, null);
        String resString = result.getPayloadAsString();
//        System.out.println(resString);
        assertTrue(resString.indexOf("<test xmlns=\"http://foo\"> foo </test>") != -1);
    }

    public void testServerClientProxy() throws Exception
    {
        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                     + "<soap:Body> <foo xmlns=\"http://foo\"></foo>" + "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/proxy", msg, null);
        String resString = result.getPayloadAsString();

        assertTrue(resString.indexOf("<foo xmlns=\"http://foo\"") != -1);
    }

    public void testProxyBodyValidation() throws Exception
    {
        doTestProxyValidation("http://localhost:" + getPorts().get(0) + "/services/proxyBodyWithValidation");
    }

    public void testProxyEnvelopeValidation() throws Exception
    {
        doTestProxyValidation("http://localhost:" + getPorts().get(0) + "/services/proxyEnvelopeWithValidation");
    }
    
    public void doTestProxyValidation(String url) throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send(url, msg, null);
        String resString = result.getPayloadAsString();
        assertTrue(resString.indexOf("Schema validation error on message") != -1);
        
        String valid = 
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap:Body> " +
                    "<echo xmlns=\"http://www.muleumo.org\">" +
                    "  <echo>test</echo>" +
                    "</echo>" 
            + "</soap:Body>" 
            + "</soap:Envelope>";
        result = client.send(url, valid, null);
        resString = result.getPayloadAsString();
        assertTrue(resString.contains("<echoResponse xmlns=\"http://www.muleumo.org\">"));
    }
    
    public void testServerClientProxyWithWsdl() throws Exception
    {
        final Latch latch = new Latch();
        ((FunctionalTestComponent) getComponent("serverClientProxyWithWsdl")).setEventCallback(new EventCallback()
        {

            public void eventReceived(MuleEventContext context, Object component) throws Exception
            {
                latch.countDown();
            }
        });

        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                     + "<soap:Body> <test xmlns=\"http://foo\"></test>" + "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/proxyWithWsdl", msg, null);
        String resString = result.getPayloadAsString();
        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(resString.indexOf("<test xmlns=\"http://foo\"") != -1);
    }

    public void testServerClientProxyWithWsdl2() throws Exception
    {
        final Latch latch = new Latch();
        ((FunctionalTestComponent) getComponent("serverClientProxyWithWsdl2")).setEventCallback(new EventCallback()
        {

            public void eventReceived(MuleEventContext context, Object component) throws Exception
            {
                latch.countDown();
            }
        });

        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                     + "<soap:Body> <test xmlns=\"http://foo\"></test>" + "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/proxyWithWsdl2", msg, null);
        String resString = result.getPayloadAsString();
        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(resString.indexOf("<test xmlns=\"http://foo\"") != -1);
    }

    public void testServerClientProxyWithTransform() throws Exception
    {
        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                     + "<soap:Body> <test xmlns=\"http://foo\"></test>" + "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/proxyWithTransform", msg, null);
        String resString = result.getPayloadAsString();
        System.out.println(resString);
        assertTrue(resString.indexOf("<transformed xmlns=\"http://foo\">") != -1);
    }

    public void testProxyWithDatabinding() throws Exception 
    {
        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap:Body><greetMe xmlns=\"http://apache.org/hello_world_soap_http/types\"><requestType>Dan</requestType></greetMe>" +
                    "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/greeter-databinding-proxy", msg, null);
        String resString = result.getPayloadAsString();
        assertTrue(resString.indexOf("greetMeResponse") != -1);
    }

    public void testProxyWithFault() throws Exception 
    {
        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap:Body><invalid xmlns=\"http://apache.org/hello_world_soap_http/types\"><requestType>Dan</requestType></invalid>" +
                    "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/greeter-proxy", msg, null);
        String resString = result.getPayloadAsString();

        assertFalse("Status code should not be 'OK' when the proxied endpoint returns a fault",
                    String.valueOf(HttpConstants.SC_OK).equals(result.getOutboundProperty("http.status")));

        assertTrue(resString.indexOf("Fault") != -1);
    }

    public void testProxyWithIntermediateTransform() throws Exception 
    {
        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap:Body><greetMe xmlns=\"http://apache.org/hello_world_soap_http/types\"><requestType>Dan</requestType></greetMe>" +
                    "</soap:Body>" + "</soap:Envelope>";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/transform-proxy", msg, null);
        String resString = result.getPayloadAsString();
        assertTrue(resString.indexOf("greetMeResponse") != -1);
    }

    public void testSoapActionRouting() throws Exception 
    {
        String msg = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap:Body> <test xmlns=\"http://foo\"></test>" + "</soap:Body>" + "</soap:Envelope>";

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("SOAPAction", "http://acme.com/transform");

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/routeBasedOnSoapAction", msg, props);
        String resString = result.getPayloadAsString();
        System.out.println(resString);
        assertTrue(resString.indexOf("<transformed xmlns=\"http://foo\">") != -1);
    }

    public void testOneWaySend() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/oneway",
            prepareOneWayTestMessage(), prepareOneWayTestProperties());
        assertEquals("", result.getPayloadAsString());

        AsyncService component = (AsyncService) getComponent("asyncService");
        assertTrue(component.getLatch().await(10000, TimeUnit.MILLISECONDS));
    }

    public void testOneWayDispatch() throws Exception
    {
        new MuleClient(muleContext).dispatch("http://localhost:" + getPorts().get(0) + "/services/oneway", prepareOneWayTestMessage(),
            prepareOneWayTestProperties());

        AsyncService component = (AsyncService) getComponent("asyncService");
        assertTrue(component.getLatch().await(10000, TimeUnit.MILLISECONDS));
    }

    /**
     * MULE-4549 ReversibleXMLStreamReader chokes on comments with ClassCastException
     * @throws Exception
     */
    public void testProxyWithCommentInRequest() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:" + getPorts().get(0) + "/services/envelope-proxy", msgWithComment, null);
        String resString = result.getPayloadAsString();
        assertTrue(resString.contains(doGoogleSearch));
    }
    
    protected String prepareOneWayTestMessage()
    {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soap:Body>"
               + "<ns:send xmlns:ns=\"http://testmodels.cxf.module.mule.org/\"><text>hello</text></ns:send>"
               + "</soap:Body>" + "</soap:Envelope>";
    }

    protected Map prepareOneWayTestProperties()
    {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("SOAPAction", "http://acme.com/oneway");
        return props;
    }
    
    protected String getConfigResources()
    {
        return "proxy-conf.xml";
    }

    @Override
    protected int getNumPortsToFind()
    {
        // TODO Auto-generated method stub
        return 1;
    }

}