/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

//package org.apache.jmeter.examples.sampler;
package com.nicholasjgreen.jmeter.sampler;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.InvokeRequest;

/**
 * Example Sampler (non-Bean version)
 *
 * JMeter creates an instance of a sampler class for every occurrence of the
 * element in every thread. [some additional copies may be created before the
 * test run starts]
 *
 * Thus each sampler is guaranteed to be called by a single thread - there is no
 * need to synchronize access to instance variables.
 *
 * However, access to class fields must be synchronized.
 *
 */
public class AwsLambdaSampler extends AbstractSampler {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(AwsLambdaSampler.class);

    // The name of the property used to hold our data
    public static final String DATA = "AwsLambdaSampler.data"; //$NON-NLS-1$
    public static final String LAMBDANAME = "AwsLambdaSampler.lambdaname";  //$NON-NLS-1$

    public AwsLambdaSampler() {
        //setName("Hi");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        boolean isOK = false; // Did sample succeed?
        String data = getData(); // Sampler data
        String response = null;

        res.setSampleLabel(getTitle());
        /*
         * Perform the sampling
         */
        res.sampleStart(); // Start timing
        try {

            /*String testString = "{\n" +
                    "  \"key3\": \"another value 3\",\n" +
                    "  \"key2\": \"another value 2\",\n" +
                    "  \"key1\": \"another value1\"\n" +
                    "}";*/

            AWSLambdaAsyncClient lambdaClient = new AWSLambdaAsyncClient();
            lambdaClient.withRegion(Region.getRegion(Regions.AP_SOUTHEAST_2));

            InvokeRequest invReq = new InvokeRequest()
                    .withFunctionName(getLambdaName())
                    .withPayload(getData())
                    .withInvocationType(InvocationType.RequestResponse);

            InvokeResult invoke = lambdaClient.invoke(invReq);
            try {
                // PRINT THE RESPONSE
                System.out.println("Status ==> " +invoke.getStatusCode());

                //String val = new String(invoke.getPayload().array(), "UTF-8");
                System.out.println("Response==> " + new String(invoke.getPayload().array()));
            } catch (Exception except) {
                System.out.println("error");
            }

            res.setSamplerData(getData());
            res.setResponseData(new String(invoke.getPayload().array()), null);
            res.setDataType(SampleResult.TEXT);

            res.setResponseCodeOK();
            res.setResponseMessage("OK");// $NON-NLS-1$
            isOK = true;

        } catch (Exception ex) {
            log.debug("", ex);
            res.setResponseCode("500");// $NON-NLS-1$
            res.setResponseMessage(ex.toString());
        }
        res.sampleEnd(); // End timing

        res.setSuccessful(isOK);

        return res;
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle() {
        return this.getName();
        //return "AWS Lambda Sampler";
    }

    /**
     * @return the data for the sample
     */
    public String getData() {
        return getPropertyAsString(DATA);
    }

    public String getLambdaName() { return getPropertyAsString(LAMBDANAME);}
}
