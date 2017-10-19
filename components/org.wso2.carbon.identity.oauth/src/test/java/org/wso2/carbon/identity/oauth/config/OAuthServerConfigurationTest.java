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

package org.wso2.carbon.identity.oauth.config;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;


@PrepareForTest({IdentityUtil.class})
public class OAuthServerConfigurationTest extends PowerMockTestCase {

    public static final String oAuth1RequestTokenUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth/request-token";
    public static final String oAuth1AuthorizeUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth/authorize-url";
    public static final String oAuth1AccessTokenUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth/access-token";
    public static final String oAuth2AuthzEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/authorize";
    public static final String oAuth2TokenEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/token";
    public static final String oAuth2RevokeEPUrll
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/revoke";
    public static final String oAuth2IntrospectEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/introspect";
    public static final String oAuth2UserInfoEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/userinfo";
    public static final String oIDCCheckSessionEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oidc/checksession";
    public static final String oIDCLogoutEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oidc/logout";
    public static final String oAuth2ConsentPage
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/authenticationendpoint/oauth2_authz.do";
    public static final String oAuth2ErrorPage
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/authenticationendpoint/oauth2_error.do";
    public static final String oIDCConsentPage
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/authenticationendpoint/oauth2_consent.do";
    public static final String oIDCLogoutConsentPage
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/authenticationendpoint/oauth2_logout_consent.do";
    public static final String oIDCLogoutPage
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/authenticationendpoint/oauth2_logout.do";
    public static final String oIDCWebFingerEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/.well-known/webfinger";
    public static final String oAuth2DCREPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/identity/connect/register";
    public static final String oAuth2JWKSPage
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/jwks";
    public static final String oIDCDiscoveryEPUrl
            = "${carbon.protocol}://${carbon.host}:${carbon.management.port}" +
            "/oauth2/oidcdiscovery";

    @BeforeMethod
    public void setUp() throws Exception {
        System.setProperty("carbon.home", System.getProperty("user.dir"));
        PowerMockito.mockStatic(IdentityUtil.class);
        PowerMockito.when(IdentityUtil.getIdentityConfigDirPath())
                .thenReturn(System.getProperty("user.dir")
                        + File.separator + "src"
                        + File.separator + "test"
                        + File.separator + "resources"
                        + File.separator + "conf");
        Field oAuthServerConfigInstance =
                OAuthServerConfiguration.class.getDeclaredField("instance");
        oAuthServerConfigInstance.setAccessible(true);
        oAuthServerConfigInstance.set(null, null);

        Field instance = IdentityConfigParser.class.getDeclaredField("parser");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {
        Assert.assertNotNull(OAuthServerConfiguration.getInstance());
    }

    @Test
    public void testGetCallbackHandlerMetaData() throws Exception {
        Set<OAuthCallbackHandlerMetaData> metadataSet =
                OAuthServerConfiguration.getInstance().getCallbackHandlerMetaData();
        Assert.assertEquals(metadataSet.toArray()[0]
                        .getClass().getName(),
                "org.wso2.carbon.identity.oauth.config.OAuthCallbackHandlerMetaData");
    }

    @Test
    public void testGetOAuth1RequestTokenUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth1RequestTokenUrl))
                .thenReturn(fillURLPlaceholdersForTest(oAuth1RequestTokenUrl));
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth1RequestTokenUrl(), fillURLPlaceholdersForTest(oAuth1RequestTokenUrl));
    }

    @Test
    public void testGetOAuth1AuthorizeUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth1AuthorizeUrl))
                .thenReturn(fillURLPlaceholdersForTest(oAuth1AuthorizeUrl));
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth1AuthorizeUrl(), fillURLPlaceholdersForTest(oAuth1AuthorizeUrl));
    }

    @Test
    public void testGetOAuth1AccessTokenUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth1AccessTokenUrl))
                .thenReturn(fillURLPlaceholdersForTest(oAuth1AccessTokenUrl));
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth1AccessTokenUrl(), fillURLPlaceholdersForTest(oAuth1AccessTokenUrl));
    }

    @Test
    public void testGetOAuth2AuthzEPUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2AuthzEPUrl))
                .thenReturn(fillURLPlaceholdersForTest(oAuth2AuthzEPUrl));
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth2AuthzEPUrl(), fillURLPlaceholdersForTest(oAuth2AuthzEPUrl));
    }

    @Test
    public void testGetOAuth2TokenEPUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2TokenEPUrl))
                .thenReturn(oAuth2TokenEPUrl);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth2TokenEPUrl(), oAuth2TokenEPUrl);
    }

    @Test
    public void testGetOAuth2DCREPUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2DCREPUrl))
                .thenReturn(oAuth2DCREPUrl);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth2DCREPUrl(), oAuth2DCREPUrl);
    }

    @Test
    public void testGetOAuth2JWKSPageUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2JWKSPage))
                .thenReturn(oAuth2JWKSPage);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOAuth2JWKSPageUrl(), oAuth2JWKSPage);
    }

    @Test
    public void testGetOidcDiscoveryUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oIDCDiscoveryEPUrl))
                .thenReturn(oIDCDiscoveryEPUrl);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOidcDiscoveryUrl(), oIDCDiscoveryEPUrl);
    }

    @Test
    public void testGetOidcWebFingerEPUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oIDCWebFingerEPUrl))
                .thenReturn(oIDCWebFingerEPUrl);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOidcWebFingerEPUrl(), oIDCWebFingerEPUrl);
    }

    @Test
    public void testGetOauth2UserInfoEPUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2UserInfoEPUrl))
                .thenReturn(oAuth2UserInfoEPUrl);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOauth2UserInfoEPUrl(), oAuth2UserInfoEPUrl);
    }

    @Test
    public void testGetOIDCConsentPageUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oIDCConsentPage))
                .thenReturn(oIDCConsentPage);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOIDCConsentPageUrl(), oIDCConsentPage);
    }

    @Test
    public void testGetOauth2ConsentPageUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2ConsentPage))
                .thenReturn(oAuth2ConsentPage);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOauth2ConsentPageUrl(), oAuth2ConsentPage);
    }

    @Test
    public void testGetOauth2ErrorPageUrl() throws Exception {
        PowerMockito.when(IdentityUtil.fillURLPlaceholders(oAuth2ErrorPage))
                .thenReturn(oAuth2ErrorPage);
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getOauth2ErrorPageUrl(), oAuth2ErrorPage);
    }

    @Test
    public void testGetOAuthTokenGenerator() throws Exception {
        Assert.assertNotNull(OAuthServerConfiguration.getInstance()
                .getOAuthTokenGenerator().accessToken());
    }

    @Test
    public void testGetTokenValueGenerator() throws Exception {
        Assert.assertNotNull(OAuthServerConfiguration.getInstance()
                .getTokenValueGenerator().generateValue());
    }

    @Test
    public void testGetIdentityOauthTokenIssuer() throws Exception {
        Assert.assertNotNull(OAuthServerConfiguration.getInstance().getIdentityOauthTokenIssuer());
    }

    @Test
    public void testGetAuthorizationCodeValidityPeriodInSeconds() throws Exception {
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getAuthorizationCodeValidityPeriodInSeconds(), 300);
    }

    @Test
    public void testGetUserAccessTokenValidityPeriodInSeconds() throws Exception {
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getUserAccessTokenValidityPeriodInSeconds(), 3600);
    }

    @Test
    public void testGetApplicationAccessTokenValidityPeriodInSeconds() throws Exception {
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getApplicationAccessTokenValidityPeriodInSeconds(), 3600);
    }

    @Test
    public void testGetRefreshTokenValidityPeriodInSeconds() throws Exception {
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getRefreshTokenValidityPeriodInSeconds(), 84600);
    }

    @Test
    public void testGetTimeStampSkewInSeconds() throws Exception {
        Assert.assertEquals(OAuthServerConfiguration.getInstance()
                .getTimeStampSkewInSeconds(), 300);
    }

    @Test
    public void testIsCacheEnabled() throws Exception {
        Assert.assertFalse(OAuthServerConfiguration.getInstance().isCacheEnabled());
    }

    @Test
    public void testIsRefreshTokenRenewalEnabled() throws Exception {
        Assert.assertTrue(OAuthServerConfiguration.getInstance()
                .isRefreshTokenRenewalEnabled());
    }

    @Test
    public void testGetSupportedGrantTypeValidators() throws Exception {
        Assert.assertTrue(OAuthServerConfiguration.getInstance()
                .getSupportedGrantTypeValidators().size() == 5);
    }

    @Test
    public void testGetSupportedResponseTypeValidators() throws Exception {
        Assert.assertTrue(OAuthServerConfiguration.getInstance()
                .getSupportedResponseTypeValidators().size() == 4);
    }

    @Test
    public void testGetSupportedResponseTypes() throws Exception {
        Assert.assertTrue(OAuthServerConfiguration.getInstance()
                .getSupportedResponseTypes().size() == 4);
    }

    @Test
    public void testGetSupportedResponseTypeNames() throws Exception {
        OAuthServerConfiguration.getInstance()
                .getSupportedResponseTypeNames();
    }

    private String fillURLPlaceholdersForTest(String url) {
        return url.replace("${carbon.protocol}", "https")
                .replace("${carbon.host}", "localhost")
                .replace("${carbon.management.port}", "9443");
    }
}
