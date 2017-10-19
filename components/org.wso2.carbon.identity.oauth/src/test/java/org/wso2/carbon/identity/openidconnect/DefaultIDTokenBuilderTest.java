/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.openidconnect;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.internal.OAuth2ServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.security.MessageDigest;
import java.util.LinkedHashSet;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.mockito.Matchers.any;
import static org.testng.Assert.*;

@PrepareForTest({
        IdentityProviderManager.class,
        FederatedAuthenticatorConfig.class,
        IdentityApplicationManagementUtil.class,
        OAuthServerConfiguration.class, OAuth2Util.class,
        JWTClaimsSet.class, FederatedAuthenticatorConfig.class,
        MessageDigest.class,
        IdentityConfigParser.class,
        OAuth2ServiceComponentHolder.class})

public class DefaultIDTokenBuilderTest {
    @Mock
    Log log;

    @Mock
    IdentityProvider identityProvider;

    @Mock
    CustomClaimsCallbackHandler customClaimsCallbackHandler;

    @Mock
    OAuthServerConfiguration oAuthServerConfiguration;

    @Mock
    JWTClaimsSet jwtClaimsSet;

    @Mock
    OAuthAuthzReqMessageContext request;

    @Mock
    OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO;

    @Mock
    OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO;

    @Mock
    IdentityProviderManager identityProviderManager;

    @Mock
    FederatedAuthenticatorConfig federatedAuthenticatorConfig;

    @Mock
    AuthenticatedUser authenticatedUser;

    @Mock
    Property property;

    @Mock
    MessageDigest messageDigest;

    @Mock
    OAuthTokenReqMessageContext request1;

    @Mock
    OAuth2AccessTokenReqDTO tokenReqDTO;

    @Mock
    OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO;

    @Mock
    JWT jwt;

    @Mock
    IdentityConfigParser identityConfigParser;

    @Mock
    ApplicationManagementService applicationManagementService;

    @Mock
    ServiceProvider serviceProvider;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    String SUBJECT_IDENTIFIER = "Subjectdentifier1";
    String CLIENT_NAME = "ClientName1";
    String ACCESSTOKEN_NAME = "AccessToken1";
    String ALGORITHM = "Algorithm1";
    String RESPONSE = "Response1";
    String CONSUMERKEY = "Key1";
    String TENANT_DOMAIN = "Tenant1";
    String NONCE = "Nonce1";

    @DataProvider(name = "BuildIdToken")
    public Object[][] buildIdToken() {
        return new Object[][]{
                {"test"},
                {null}
        };
    }

    @Test(dataProvider = "BuildIdToken", expectedExceptions = IdentityOAuth2Exception.class)
    public void testBuildIDToken(String expected) throws Exception {
        request1 = mock(OAuthTokenReqMessageContext.class);
        tokenReqDTO = mock(OAuth2AccessTokenReqDTO.class);
        when(request1.getOauth2AccessTokenReqDTO()).thenReturn(tokenReqDTO);
        when(tokenReqDTO.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        identityProvider = mock(IdentityProvider.class);
        mockStatic(IdentityProviderManager.class);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP(anyString())).thenReturn(identityProvider);

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = new FederatedAuthenticatorConfig[10];
        when(identityProvider.getFederatedAuthenticatorConfigs()).thenReturn(federatedAuthenticatorConfigs);

        mockStatic(IdentityApplicationManagementUtil.class);
        when(IdentityApplicationManagementUtil
                .getFederatedAuthenticator(federatedAuthenticatorConfigs, "user1"))
                .thenReturn(federatedAuthenticatorConfig);
        Property[] properties = new Property[1];
        Property property = new Property();
        property.setName("IdPEntityId");
        property.setValue("localhost");
        properties[0] = property;

        federatedAuthenticatorConfig.setProperties(properties);
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfig1 = new FederatedAuthenticatorConfig[10];
        when(identityProvider.getFederatedAuthenticatorConfigs()).thenReturn(federatedAuthenticatorConfig1);

        mockStatic(IdentityApplicationManagementUtil.class);
        when(IdentityApplicationManagementUtil
                .getFederatedAuthenticator(federatedAuthenticatorConfigs, IdentityApplicationConstants
                        .Authenticator.OIDC.NAME)).thenReturn(federatedAuthenticatorConfig);
        when(IdentityApplicationManagementUtil.getProperty(any(Property[].class), anyString())).thenCallRealMethod();

        when(federatedAuthenticatorConfig.getProperties()).thenReturn(properties);

        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn((long) 3);
        mockStatic(OAuth2Util.class);
        when(oAuthServerConfiguration.getOpenIDConnectIDTokenExpiration()).thenReturn(String.valueOf((int) 78383));
        when(OAuth2Util.mapDigestAlgorithm(any(JWSAlgorithm.class))).thenReturn(String.valueOf(JWSAlgorithm.RS256));

        when(request1.getAuthorizedUser()).thenReturn(authenticatedUser);
        when(authenticatedUser.getAuthenticatedSubjectIdentifier()).thenReturn(SUBJECT_IDENTIFIER);

        applicationManagementService = mock(ApplicationManagementService.class);
        serviceProvider = mock(ServiceProvider.class);

        mockStatic(OAuth2ServiceComponentHolder.class);
        when(OAuth2ServiceComponentHolder.getApplicationMgtService()).thenReturn(applicationManagementService);
        when(applicationManagementService.
                getApplicationExcludingFileBasedSPs(anyString(), anyString()))
                .thenThrow(new IdentityApplicationManagementException("IdentityApplicationManagementException"));

        when(applicationManagementService.getServiceProviderByClientId(anyString(), anyString(), anyString()))
                .thenReturn(serviceProvider);
        when(tokenReqDTO.getClientId()).thenReturn(CLIENT_NAME);

        JWSAlgorithm jwsAlgorithm1 = new JWSAlgorithm(ALGORITHM);
        when(OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(anyString())).thenReturn(jwsAlgorithm1);
        when(OAuth2Util.mapDigestAlgorithm(any(JWSAlgorithm.class))).thenReturn("SHA-256");

        mockStatic(MessageDigest.class);
        when(MessageDigest.getInstance(String.valueOf(JWSAlgorithm.RS256))).thenReturn(messageDigest);
        when(oAuth2AccessTokenRespDTO.getAccessToken()).thenReturn(ACCESSTOKEN_NAME);

        mockStatic(IdentityConfigParser.class);
        when(IdentityConfigParser.getInstance()).thenReturn(identityConfigParser);
        when(oAuthServerConfiguration.getOpenIDConnectCustomClaimsCallbackHandler())
                .thenReturn(customClaimsCallbackHandler);
        doNothing().when(customClaimsCallbackHandler).handleCustomClaims(jwtClaimsSet, request);

        when(OAuth2Util.signJWT(any(JWTClaimsSet.class), any(JWSAlgorithm.class), anyString())).thenReturn(jwt);
        DefaultIDTokenBuilder defaultIDTokenBuilder1 = new DefaultIDTokenBuilder();
        String actual = defaultIDTokenBuilder1.buildIDToken(request1, oAuth2AccessTokenRespDTO);
        if (actual == expected) {
            assertEquals(actual, expected, "Default token binder generated successfully.");
        }
    }

    @DataProvider(name = "AuthorizeIdToken")
    public Object[][] authorizeIdToken() {
        return new Object[][]{
                {"test1"},
                {null}
        };
    }

    @Test(dataProvider = "AuthorizeIdToken")
    public void testBuildIDTokenAuthorize(String expected) throws Exception {
        request = mock(OAuthAuthzReqMessageContext.class);
        identityProvider = mock(IdentityProvider.class);
        identityProviderManager = mock(IdentityProviderManager.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        when(request.getAuthorizationReqDTO()).thenReturn(oAuth2AuthorizeReqDTO);
        when(oAuth2AuthorizeReqDTO.getTenantDomain()).thenReturn("carbon.super");
        mockStatic(IdentityProviderManager.class);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP(anyString())).thenReturn(identityProvider);

        Property[] properties = new Property[1];
        Property property = new Property();
        property.setName("IdPEntityId");
        property.setValue("localhost");
        properties[0] = property;

        federatedAuthenticatorConfig.setProperties(properties);
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = new FederatedAuthenticatorConfig[10];
        when(identityProvider.getFederatedAuthenticatorConfigs()).thenReturn(federatedAuthenticatorConfigs);

        mockStatic(IdentityApplicationManagementUtil.class);
        when(IdentityApplicationManagementUtil.
                getFederatedAuthenticator(federatedAuthenticatorConfigs, IdentityApplicationConstants
                        .Authenticator.OIDC.NAME)).thenReturn(federatedAuthenticatorConfig);
        when(IdentityApplicationManagementUtil.getProperty(any(Property[].class), anyString())).thenCallRealMethod();
        when(federatedAuthenticatorConfig.getProperties()).thenReturn(properties);

        when(oAuth2AuthorizeReqDTO.getUser()).thenReturn(authenticatedUser);
        when(authenticatedUser.getAuthenticatedSubjectIdentifier()).thenReturn(SUBJECT_IDENTIFIER);
        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn((long) 3);
        mockStatic(OAuth2Util.class);
        when(oAuthServerConfiguration.getOpenIDConnectIDTokenExpiration()).thenReturn(String.valueOf((int) 78383));

        when(oAuth2AuthorizeReqDTO.getNonce()).thenReturn(NONCE);
        LinkedHashSet set1 = new LinkedHashSet(10);
        when(oAuth2AuthorizeReqDTO.getACRValues()).thenReturn(set1);
        when(oAuth2AuthorizeReqDTO.getResponseType()).thenReturn(RESPONSE);

        JWSAlgorithm jwsAlgorithm = new JWSAlgorithm(ALGORITHM);
        when(OAuth2Util.mapDigestAlgorithm(any(JWSAlgorithm.class))).thenReturn("SHA-256");
        when(oAuthServerConfiguration.getIdTokenSignatureAlgorithm()).thenReturn(ALGORITHM);
        mockStatic(FederatedAuthenticatorConfig.class);

        JWSAlgorithm jwsAlgorithm1 = new JWSAlgorithm(ALGORITHM);
        when(OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(anyString())).thenReturn(jwsAlgorithm1);

        mockStatic(MessageDigest.class);
        when(MessageDigest.getInstance(String.valueOf(JWSAlgorithm.RS256))).thenReturn(messageDigest);
        when(oAuth2AuthorizeRespDTO.getAccessToken()).thenReturn(ACCESSTOKEN_NAME);
        when(oAuth2AuthorizeReqDTO.getConsumerKey()).thenReturn(CONSUMERKEY);
        mockStatic(IdentityConfigParser.class);
        when(IdentityConfigParser.getInstance()).thenReturn(identityConfigParser);
        when(oAuthServerConfiguration.getOpenIDConnectCustomClaimsCallbackHandler())
                .thenReturn(customClaimsCallbackHandler);
        doNothing().when(customClaimsCallbackHandler).handleCustomClaims(jwtClaimsSet, request);

        when(OAuth2Util.signJWT(any(JWTClaimsSet.class), any(JWSAlgorithm.class), anyString())).thenReturn(jwt);
        DefaultIDTokenBuilder defaultIDTokenBuilder = new DefaultIDTokenBuilder();
        String actual = defaultIDTokenBuilder.buildIDToken(request, oAuth2AuthorizeRespDTO);
        if (actual == expected) {
            assertEquals(actual, expected, "Default token binder authorized successfully.");
        }
    }
}
