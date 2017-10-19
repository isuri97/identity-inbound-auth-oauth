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
package org.wso2.carbon.identity.oauth2;

import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth.event.OAuthEventInterceptor;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth.tokenprocessor.PlainTextPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.authz.AuthorizationHandlerManager;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.*;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;
import org.wso2.carbon.identity.oauth2.token.AccessTokenIssuer;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;

import static com.hazelcast.client.nearcache.ClientNearCacheType.Map;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.Assert.*;

/**
 * This class tests the OAuth2Service class.
 */
@PrepareForTest({
        OAuth2Util.class,
        AuthorizationHandlerManager.class,
        OAuth2Service.class,
        IdentityTenantUtil.class,
        OAuthServerConfiguration.class,
        AccessTokenIssuer.class,
        OAuthComponentServiceHolder.class,
        OAuthUtil.class,
        OAuthCache.class
})
public class OAuth2ServiceTest extends PowerMockTestCase {

    @Mock
    private OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO;

    @Mock
    private AuthorizationHandlerManager authorizationHandlerManager;

    @Mock
    OAuth2AuthorizeRespDTO mockedOAuth2AuthorizeRespDTO;

    @Mock
    OAuthAppDAO oAuthAppDAO;

    @Mock
    OAuthAppDO oAuthAppDO;

    @Mock
    AuthenticatedUser authenticatedUser;

    @Mock
    OAuthEventInterceptor oAuthEventInterceptorProxy;

    @Mock
    private OAuthServerConfiguration oAuthServerConfiguration;

    @Mock
    OAuthComponentServiceHolder oAuthComponentServiceHolder;

    @Mock
    OAuthCache oAuthCache;

    OAuth2Service oAuth2Service;
    static final String clientId = "IbWwXLf5MnKSY6x6gnR_7gd7f1wa";
    TokenPersistenceProcessor persistenceProcessor = new PlainTextPersistenceProcessor();

    @BeforeMethod
    public void setUp() throws Exception {
        oAuth2Service = new OAuth2Service();
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    /**
     * DataProvider: grantType, callbackUrl, tenantDomain, callbackURI
     */
    @DataProvider(name = "ValidateClientInfoDataProvider")
    public Object[][] validateClientDataProvider() {
        return new Object[][]{
                {null, null, null, null},
                {"dummyGrantType", "dummyCallBackUrl", "carbon.super", null},
                {"dummyGrantType", "dummyCallBackUrl", "carbon.super", "dummyCallBackURI"},
                {"dummyGrantType", "regexp=dummyCallBackUrl", "carbon.super", "dummyCallBackURI"},
                {"dummyGrantType", "regexp=dummyCallBackUrl", "carbon.super", "dummyCallBackUrl"},
                {"dummyGrantType", "dummyCallBackUrl", "carbon.super", "dummyCallBackUrl"}
        };
    }

    @Test
    public void testAuthorize() throws Exception {
        mockStatic(AuthorizationHandlerManager.class);
        when(AuthorizationHandlerManager.getInstance()).thenReturn(authorizationHandlerManager);
        when(authorizationHandlerManager.handleAuthorization((OAuth2AuthorizeReqDTO) anyObject())).
                thenReturn(mockedOAuth2AuthorizeRespDTO);
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO = oAuth2Service.authorize(oAuth2AuthorizeReqDTO);
        assertNotNull(oAuth2AuthorizeRespDTO);
    }

    @Test
    public void testAuthorizeWithException() throws IdentityOAuth2Exception {
        String callbackUrl = "dummyCallBackUrl";
        mockStatic(AuthorizationHandlerManager.class);
        when(oAuth2AuthorizeReqDTO.getCallbackUrl()).thenReturn(callbackUrl);
        when(AuthorizationHandlerManager.getInstance()).thenThrow(new IdentityOAuth2Exception
                ("Error while creating AuthorizationHandlerManager instance"));
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO = oAuth2Service.authorize(oAuth2AuthorizeReqDTO);
        assertNotNull(oAuth2AuthorizeRespDTO);
    }

    @Test(dataProvider = "ValidateClientInfoDataProvider")
    public void testValidateClientInfo(String grantType, String callbackUrl, String tenantDomain, String callbackURI)
            throws Exception {
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(3600L);
        whenNew(OAuthAppDAO.class).withNoArguments().thenReturn(oAuthAppDAO);
        when(oAuthAppDAO.getAppInformation(clientId)).thenReturn(oAuthAppDO);
        when(oAuthAppDO.getGrantTypes()).thenReturn(grantType);
        when(oAuthAppDO.getCallbackUrl()).thenReturn(callbackUrl);
        when(oAuthAppDO.getUser()).thenReturn(authenticatedUser);
        when(authenticatedUser.getTenantDomain()).thenReturn(tenantDomain);
        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        OAuth2ClientValidationResponseDTO oAuth2ClientValidationResponseDTO = oAuth2Service.
                validateClientInfo(clientId, callbackURI);
        assertNotNull(oAuth2ClientValidationResponseDTO);
    }

    @Test
    public void testInvalidOAuthClientException() throws Exception {
        String callbackUrI = "dummyCallBackURI";
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(3600L);
        whenNew(OAuthAppDAO.class).withNoArguments().thenReturn(oAuthAppDAO);
        when(oAuthAppDAO.getAppInformation(clientId)).thenThrow
                (new InvalidOAuthClientException("Cannot find an application associated with the given consumer key"));
        OAuth2ClientValidationResponseDTO oAuth2ClientValidationResponseDTO = oAuth2Service.
                validateClientInfo(clientId, callbackUrI);
        assertNotNull(oAuth2ClientValidationResponseDTO);
        assertEquals(oAuth2ClientValidationResponseDTO.getErrorCode(), "invalid_client");
        assertFalse(oAuth2ClientValidationResponseDTO.isValidClient());
    }

    @Test
    public void testIdentityOAuth2Exception() throws Exception {
        String callbackUrI = "dummyCallBackURI";
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(3600L);
        whenNew(OAuthAppDAO.class).withNoArguments().thenReturn(oAuthAppDAO);
        when(oAuthAppDAO.getAppInformation(clientId)).thenThrow
                (new IdentityOAuth2Exception("Error while retrieving the app information"));
        OAuth2ClientValidationResponseDTO oAuth2ClientValidationResponseDTO = oAuth2Service.
                validateClientInfo(clientId, callbackUrI);
        assertNotNull(oAuth2ClientValidationResponseDTO);
        assertEquals(oAuth2ClientValidationResponseDTO.getErrorCode(), "server_error");
        assertFalse(oAuth2ClientValidationResponseDTO.isValidClient());
    }

    @Test
    public void testIssueAccessToken() throws IdentityException {

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        AccessTokenIssuer accessTokenIssuer = mock(AccessTokenIssuer.class);
        mockStatic(AccessTokenIssuer.class);
        when(AccessTokenIssuer.getInstance()).thenReturn(accessTokenIssuer);
        when(accessTokenIssuer.issue(any(OAuth2AccessTokenReqDTO.class))).thenReturn(tokenRespDTO);
        assertNotNull(oAuth2Service.issueAccessToken(new OAuth2AccessTokenReqDTO()));

        when(accessTokenIssuer.issue(any(OAuth2AccessTokenReqDTO.class)))
                .thenThrow(new InvalidOAuthClientException(""));
        assertEquals(oAuth2Service.issueAccessToken(new OAuth2AccessTokenReqDTO())
                .getErrorMsg(), "Invalid Client");
    }


    @Test
    public void testExceptionForIssueAccesstoken() throws IdentityException {

        AccessTokenIssuer accessTokenIssuer = mock(AccessTokenIssuer.class);
        mockStatic(AccessTokenIssuer.class);
        when(AccessTokenIssuer.getInstance()).thenReturn(accessTokenIssuer);
        when(accessTokenIssuer.issue(any(OAuth2AccessTokenReqDTO.class)))
                .thenThrow(new IdentityOAuth2Exception(""));
        assertEquals(oAuth2Service.issueAccessToken(new OAuth2AccessTokenReqDTO())
                .getErrorCode(), "server_error");
    }

    @Test
    public void testIsPKCESupportEnabled() {

        OAuthServerConfiguration oAuthServerConfiguration = mock(OAuthServerConfiguration.class);
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(3600L);

        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        mockStatic(OAuth2Util.class);
        when(OAuth2Util.isPKCESupportEnabled()).thenReturn(true);
        assertTrue(oAuth2Service.isPKCESupportEnabled());
    }

    @Test
    public void testRevokeTokenByOAuthClientWithRefreshToken() throws Exception {

        setUpRevokeToken();

        RefreshTokenValidationDataDO refreshTokenValidationDataDO = new RefreshTokenValidationDataDO();
        refreshTokenValidationDataDO.setGrantType(GrantType.REFRESH_TOKEN.toString());
        refreshTokenValidationDataDO.setAccessToken("testAccessToken");
        refreshTokenValidationDataDO.setAuthorizedUser(authenticatedUser);
        refreshTokenValidationDataDO.setScope(new String[]{"test"});
        refreshTokenValidationDataDO.setRefreshTokenState(OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE);

        TokenMgtDAO tokenMgtDAO = mock(TokenMgtDAO.class);    //mock methods for tokenMgtDAO
        when(tokenMgtDAO.validateRefreshToken(anyString(), anyString())).thenReturn(refreshTokenValidationDataDO);
        whenNew(TokenMgtDAO.class).withAnyArguments().thenReturn(tokenMgtDAO);
        doNothing().when(tokenMgtDAO).revokeTokens(any(String[].class));

        OAuthRevocationRequestDTO revokeRequestDTO = new OAuthRevocationRequestDTO();
        assertEquals(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).getErrorMsg(),
                "Invalid revocation request");

        revokeRequestDTO.setConsumerKey("testCosumerKey");
        revokeRequestDTO.setToken("testToken");
        revokeRequestDTO.setToken_type(GrantType.REFRESH_TOKEN.toString());
        assertFalse(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).isError());

        revokeRequestDTO.setToken_type(null);
        when(oAuthCache.getValueFromCache(any(OAuthCacheKey.class))).thenReturn(null);
        mockStatic(OAuthCache.class);
        when(OAuthCache.getInstance()).thenReturn(oAuthCache);
        assertFalse(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).isError());

        refreshTokenValidationDataDO.setRefreshTokenState(OAuthConstants.TokenStates.TOKEN_STATE_EXPIRED);
        when(tokenMgtDAO.validateRefreshToken(anyString(), anyString())).thenReturn(refreshTokenValidationDataDO);
        assertNotNull(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO));
    }

    @Test
    public void testRevokeTokenByOAuthClientWithAccesstoken() throws Exception {
        setUpRevokeToken();

        AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
        when(authenticatedUser.toString()).thenReturn("testAuthenticatedUser");

        AccessTokenDO accessTokenDO = new AccessTokenDO();
        accessTokenDO.setConsumerKey("testCosumerKey");
        accessTokenDO.setAuthzUser(authenticatedUser);


        TokenMgtDAO tokenMgtDAO = mock(TokenMgtDAO.class);    //mock methods for tokenMgtDAO
        doNothing().when(tokenMgtDAO).revokeTokens(any(String[].class));
        when(tokenMgtDAO.retrieveAccessToken(anyString(), anyBoolean())).thenReturn(accessTokenDO);
        whenNew(TokenMgtDAO.class).withAnyArguments().thenReturn(tokenMgtDAO);


        OAuthRevocationRequestDTO revokeRequestDTO = new OAuthRevocationRequestDTO();
        revokeRequestDTO.setConsumerKey("testCosumerKey");
        revokeRequestDTO.setToken("testToken");
        revokeRequestDTO.setToken_type(GrantType.CLIENT_CREDENTIALS.toString());

        when(oAuthCache.getValueFromCache(any(OAuthCacheKey.class))).thenReturn(accessTokenDO);
        mockStatic(OAuthCache.class);
        when(OAuthCache.getInstance()).thenReturn(oAuthCache);

        oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO);
        assertFalse(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).isError());

//        when(oAuthCache.getValueFromCache(any(OAuthCacheKey.class))).thenReturn(null);
//        assertFalse(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).isError());

        when(OAuth2Util.authenticateClient(anyString(), anyString())).thenThrow(new InvalidOAuthClientException(" "));
        assertEquals(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).getErrorMsg(), "Unauthorized Client");
    }

    @Test
    public void testIdentityOAuth2ExceptionForRevokeTokenByOAuthClient() throws Exception {

        when(oAuthEventInterceptorProxy.isEnabled()).thenReturn(true);
        doThrow(new IdentityOAuth2Exception("")).when(oAuthEventInterceptorProxy)
                .onPreTokenRevocationByClient(any(OAuthRevocationRequestDTO.class), anyMap());
        when(oAuthComponentServiceHolder.getOAuthEventInterceptorProxy()).thenReturn(oAuthEventInterceptorProxy);
        mockStatic(OAuthComponentServiceHolder.class);
        when(OAuthComponentServiceHolder.getInstance()).thenReturn(oAuthComponentServiceHolder);

        mockStatic(OAuthServerConfiguration.class);
        when(oAuthServerConfiguration.getPersistenceProcessor()).thenReturn(persistenceProcessor);
        TokenMgtDAO tokenMgtDAO = mock(TokenMgtDAO.class);    //mock methods for tokenMgtDAO
        whenNew(TokenMgtDAO.class).withAnyArguments().thenReturn(tokenMgtDAO);

        OAuthRevocationRequestDTO revokeRequestDTO = new OAuthRevocationRequestDTO();
        revokeRequestDTO.setConsumerKey("testCosumerKey");
        revokeRequestDTO.setToken("testToken");
        String result = "Error occurred while revoking authorization grant for applications";
        assertEquals(oAuth2Service.revokeTokenByOAuthClient(revokeRequestDTO).getErrorMsg(), result);
    }


    public void setUpRevokeToken() throws Exception {

        when(oAuthEventInterceptorProxy.isEnabled()).thenReturn(true);
        doNothing().when(oAuthEventInterceptorProxy).onPostTokenRevocationByClient
                (any(OAuthRevocationRequestDTO.class), any(OAuthRevocationResponseDTO.class), any(AccessTokenDO.class),
                        any(RefreshTokenValidationDataDO.class), any(HashMap.class));

        when(oAuthComponentServiceHolder.getOAuthEventInterceptorProxy()).thenReturn(oAuthEventInterceptorProxy);
        mockStatic(OAuthComponentServiceHolder.class);
        when(OAuthComponentServiceHolder.getInstance()).thenReturn(oAuthComponentServiceHolder);

        TokenPersistenceProcessor persistenceProcessor = new PlainTextPersistenceProcessor();

        when(authenticatedUser.toString()).thenReturn("testAuthenticatedUser");

        mockStatic(OAuthServerConfiguration.class);
        when(oAuthServerConfiguration.getPersistenceProcessor()).thenReturn(persistenceProcessor);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(3600L);
        when(oAuthServerConfiguration.isRevokeResponseHeadersEnabled()).thenReturn(true);

        mockStatic(OAuth2Util.class);
        when(OAuth2Util.authenticateClient(anyString(), anyString())).thenReturn(true);
        when(OAuth2Util.buildScopeString(any(String[].class))).thenReturn("test");

        mockStatic(OAuthUtil.class);
        doNothing().when(OAuthUtil.class, "clearOAuthCache", anyString());
        doNothing().when(OAuthUtil.class, "clearOAuthCache", anyString(), anyString());
        doNothing().when(OAuthUtil.class, "clearOAuthCache", anyString(), anyString(), anyString());
    }

}
