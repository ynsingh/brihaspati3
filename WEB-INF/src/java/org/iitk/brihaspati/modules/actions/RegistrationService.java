package org.iitk.brihaspati.modules.actions;
/*
 * @(#)RegistrationService.java        
 *
 *  Copyright (c) 2011 ETRG,IIT Kanpur. 
 *  All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or 
 *  without modification, are permitted provided that the following 
 *  conditions are met:
 * 
 *  Redistributions of source code must retain the above copyright  
 *  notice, this  list of conditions and the following disclaimer.
 * 
 *  Redistribution in binary form must reproducuce the above copyright 
 *  notice, this list of conditions and the following disclaimer in 
 *  the documentation and/or other materials provided with the 
 *  distribution.
 * 
 * 
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL ETRG OR ITS CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL,SPECIAL, EXEMPLARY, OR 
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 *  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *  Contributors : members of ETRG, IIT Kanpur  
 *  
 */


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import org.joda.time.YearMonthDay;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;

import org.iitk.brihaspati.modules.utils.ErrorDumpUtil;

import org.apache.velocity.context.Context;
import org.apache.turbine.util.RunData;



/**
 * Consolidates business logic from the UI code for Registration activities.
 * 
 * 
 * @author Nagendra Kumar singh
 * @author http://sites.google.com/site/nksinghiitk/
 *
 */
public class RegistrationService {
	private static final Logger log = Logger.getLogger(RegistrationService.class);
	
	/**
	 * Perform discovery on the User-Supplied identifier and return the
	 * DiscoveryInformation object that results from Association with the
	 * OP. This will probably be needed by the caller (stored in Session
	 * perhaps?).
	 * 
	 * I'm not thrilled about ConsumerManager being static, but it is
	 * very important to openid4java that the ConsumerManager object be the
	 * same instance all through a conversation (discovery, auth request, 
	 * auth response) with the OP. I didn't dig terribly deeply, but suspect
	 * that part of the key exchange or the nonce uses the ConsumerManager's
	 * hash, or some other instance-specific construct to do its thing.
	 * 
	 * @param userSuppliedIdentifier The User-Supplied identifier. It may already
	 *  be normalized.
	 *
	 *  @return DiscoveryInformation - The resulting DisoveryInformation object
	 *  returned by openid4java following successful association with the OP.
	 */
	@SuppressWarnings("unchecked")
	public static DiscoveryInformation performDiscoveryOnUserSuppliedIdentifier(String userSuppliedIdentifier) {
		DiscoveryInformation ret = null;
		//
		ConsumerManager consumerManager = getConsumerManager();
		try {
		// Perform discover on the User-Supplied Identifier
		List<DiscoveryInformation> discoveries = consumerManager.discover(userSuppliedIdentifier);
		// Pass the discoveries to the associate() method...
		ret = consumerManager.associate(discoveries);

		} catch (DiscoveryException e) {
			String message = "Error occurred during discovery!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		catch (Exception ex){
			String message = "Error occurred during the method discovery information!";
			log.error(message, ex);
		}
		return ret;
	}
	/**
	 * Create an OpenID Auth Request, using the DiscoveryInformation object
	 * return by the openid4java library.
	 * 
	 * This method also uses the Simple Registration Extension to grant
	 * the Relying Party (RP).
	 * 
	 * @param discoveryInformation The DiscoveryInformation that should have
	 *  been previously obtained from a call to 
	 *  performDiscoveryOnUserSuppliedIdentifier().
	 *  
	 *  @param returnToUrl The URL to which the OP will redirect once the
	 *   authentication call is complete.
	 *  
	 * @return AuthRequest - A "good-to-go" AuthRequest object packed with all
	 *  kinds of great OpenID goodies for the OpenID Provider (OP). The caller
	 *  must take this object and forward it on to the OP. Or call
	 *  processAuthRequest() - part of this Service Class.
	 */
	public static AuthRequest createOpenIdAuthRequest(DiscoveryInformation discoveryInformation, String returnToUrl) {
		//AuthRequest ret = null;
		AuthRequest ret;
		//
		try {
			// Create the AuthRequest object
			ret = getConsumerManager().authenticate(discoveryInformation, returnToUrl);
			// Create the Simple Registration Request
			SRegRequest sRegRequest = SRegRequest.createFetchRequest();
			sRegRequest.addAttribute("email", false);
			sRegRequest.addAttribute("fullname", false);
			sRegRequest.addAttribute("dob", false);
			sRegRequest.addAttribute("postcode", false);
			ret.addExtension(sRegRequest);
			
		} catch (Exception e) {
			String message = "Exception occurred while building AuthRequest object!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		return ret;
	}
	
	/**
	 * Processes the returned information from an authentication request
	 * from the OP.
	 * 
	 * @param discoveryInformation DiscoveryInformation that was created earlier
	 *  in the conversation (by openid4java). This will need to be verified with
	 *  openid4java to make sure everything went smoothly and there are no
	 *  possible problems. This object was probably stored in session and retrieved
	 *  for use in calling this method.
	 *  
	 * @param pageParameters PageParameters passed to the page handling the
	 *  return verificaion.
	 *  
	 * @param returnToUrl The "return to" URL that was passed to the OP. It must
	 *  match exactly, or openid4java will issue a verification failed message
	 *  in the logs.
	 *  
	 * @return RegistrationModel - null if there was a problem, or a RegistrationModel
	 *  object, with parameters filled in as compeletely as possible from the
	 *  information available from the OP. If you are using MyOpenID, most of the
	 *  time what is returned is from your "Default" profile, so if you need more 
	 *  information returned, make sure your Default profile is completely filled
	 *  out.
	 */
	//public static RegistrationModel processReturn(DiscoveryInformation discoveryInformation, RunData data, String returnToUrl) {
//	public static RegistrationModel processReturn(DiscoveryInformation discoveryInformation, PageParameters pageParameters, String returnToUrl) {
	public static RegistrationModel processReturn(DiscoveryInformation discoveryInformation, HttpServletRequest httpReq, String returnToUrl) {
		RegistrationModel ret = null;
		// Verify the Information returned from the OP
		/// This is required according to the spec
//		ParameterParser pp=data.getParameters();
		ParameterList response =new ParameterList(httpReq.getParameterMap());
//		ParameterList response = new ParameterList(pageParameters);
		try {
			VerificationResult verificationResult = getConsumerManager().verify(returnToUrl, response, discoveryInformation);
			Identifier verifiedIdentifier = verificationResult.getVerifiedId();
			if (verifiedIdentifier != null) {
				AuthSuccess authSuccess = (AuthSuccess)verificationResult.getAuthResponse();
				if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
					MessageExtension extension = authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);
					if (extension instanceof SRegResponse) {
						ret = new RegistrationModel();
						ret.setOpenId(verifiedIdentifier.getIdentifier());
						SRegResponse sRegResponse = (SRegResponse)extension;
						String value = sRegResponse.getAttributeValue("dob");
						if (value != null) {
						  ret.setDateOfBirth(new YearMonthDay(value).toDateMidnight().toDate());
						}
						value = sRegResponse.getAttributeValue("email");
						if (value != null) {
						  ret.setEmailAddress(value);
						}
						value = sRegResponse.getAttributeValue("fullname");
						if (value != null) {
						  ret.setFullName(value);
						}
						value = sRegResponse.getAttributeValue("postcode");
						if (value != null) {
						  ret.setZipCode(value);
						}
					}
				}
			}
		} catch (Exception e) {
			String message = "Exception occurred while verifying response!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		return ret;
	}

	private static ConsumerManager consumerManager;
	/**
	 * Retrieves an instance of the ConsumerManager object. It is static
	 * (see note in Class-level JavaDoc comments above) because openid4java
	 * likes it that way.
	 * 
	 * Note: if you are planning to debug the code, set the lifespan parameter
	 * of the InMemoryNonceVerifier high enough to outlive your debug cycle, or
	 * you may notice Nonce Verification errors. Depending on where you are
	 * debugging, this might pose an artificial problem for you (of your own
	 * making) that has nothing to do with either your code or openid4java.
	 * 
	 * @return ConsumerManager - The ConsumerManager object that handles
	 *  communication with the openid4java API.
	 */
	private static ConsumerManager getConsumerManager() {
		try {
			if (consumerManager == null) {
				consumerManager = new ConsumerManager();
				consumerManager.setAssociations(new InMemoryConsumerAssociationStore());
				consumerManager.setNonceVerifier(new InMemoryNonceVerifier(10000));
			}
		} catch (ConsumerException e) {
			String message = "Exception creating ConsumerManager!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		return consumerManager;
	}
  /**
   * Generates the returnToUrl parameter that is passed to the OP. The
   * User Agent (i.e., the browser) will be directed to this page following
   * authentication.
   * 
   * @param representedPage The RegistrationPage object whose cover is to be
   *  cracked open to get at the raw HttpServlet goodies inside.
   *  
   * @return String - the returnToUrl to be used for the authentication request.
   */
  public static String getReturnToUrl(RunData data, Context context) {
	String rturl=data.getServerScheme()+"://"+data.getServerName()+":"+data.getServerPort()+"/brihaspati/servlet/brihaspati/action/openidResponse?is_return=true";
//    return "http://172.26.82.17:8080/brihaspati/servlet/brihaspati/action/openidResponse";
   // return sScheme+"://"+sName+":"+sPort+"/brihaspati/servlet/brihaspati/action/openidResponse?is_return=true";
    return rturl;
  }
}
