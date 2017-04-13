/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.mycompany.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives
 * 
 * @author Jean Deruelle
 *
 */
public class HelloSipWorld extends SipServlet {

	private static Log logger = LogFactory.getLog(HelloSipWorld.class);

	@Resource
	private static SipFactory sipFactory;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the HelloSipWorld servlet has been started");
		super.init(servletConfig);
	}
	//HashMap<String,SipServletResponse> register401=new HashMap<String, SipServletResponse>();

	HashMap<String,Address> registeredUsersToIp=new HashMap<String, Address>();

	@Override
	protected  void doRegister(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got request:\n"
				+ request.toString());
		B2buaHelper helper=request.getB2buaHelper();

		Address addr=request.getAddressHeader("Contact");
		String user=((SipURI)request.getFrom().getURI()).getUser();
		registeredUsersToIp.put(user,addr);

		SipServletRequest newRequest;
		/*if(helper.getLinkedSession(request.getSession())!=null){
			logger.info("this request has exits！！！！");
			newRequest=helper.getLinkedSession(request.getSession()).createRequest(request.getMethod());*/

		/*}else {*/
		newRequest = helper.createRequest(request);
		logger.info("WARNING:" +
				"session1:"+request.getSession()+
				"session2"+newRequest.getSession());
		helper.linkSipSessions(request.getSession(), newRequest.getSession());
		logger.info("display::"+newRequest.getFrom().getDisplayName());
		/*if(register401.containsKey(newRequest.getFrom().getDisplayName())){
			logger.info("set authrization massage！！！！");
			newRequest.addAuthHeader(register401.get(newRequest.getFrom().getDisplayName()),newRequest.getFrom().getDisplayName(),"test");
		}*/
		SipURI asterisk=sipFactory.createSipURI(null,"10.206.16.36");
		asterisk.setPort(5060);
		newRequest.setRequestURI(asterisk);
		//Address asterisk2=sipFactory.createAddress("192.168.183.128");
		newRequest.pushRoute(asterisk);
		/*if(helper.getLinkedSession(request.getSession()).isValid()){
			newRequest.addHeader("Authorization",request.getHeader("Authorization"));
		}*/
		newRequest.addHeader("User-Agent",request.getHeader("User-Agent"));
		newRequest.addHeader("Organization",request.getHeader("Organization"));
		newRequest.send();

	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException{
		logger.info("Got response:\n"+
		 response);
		B2buaHelper helper=response.getRequest().getB2buaHelper();
		if(response.getStatus()==SipServletResponse.SC_UNAUTHORIZED) {
			SipServletRequest authRequest=response.getSession().createRequest(response.getMethod());
			//authRequest.getFrom().setParameter("tag",response.getRequest().getFrom().getParameter("tag"));
			authRequest.addAuthHeader(response,authRequest.getFrom().getDisplayName(),"test");
			logger.info("send the authed message to the asterisk!!!");
			if(response.getMethod()=="INVITE"){
				SipServletRequest request=response.getRequest();
				authRequest.setContent(request.getContent(), request.getContentType());
			}
			authRequest.send();
			return;
		}
		response.getSession().setAttribute("lastResponse",response);
		logger.info("response session:"+response.getRequest().getSession());
		SipSession OrigiSession=helper.getLinkedSession(response.getRequest().getSession());
		logger.info("has done here!"+OrigiSession);
		SipServletResponse newResponse=helper.createResponseToOriginalRequest(OrigiSession,response.getStatus(),response.getReasonPhrase());
		newResponse.getSession().setAttribute("lastResponse",newResponse);
		newResponse.addHeader("Server",response.getHeader("Server"));
		newResponse.addHeader("Allow",response.getHeader("Allow"));
		newResponse.addHeader("Supported",response.getHeader("Supported"));
		/*if(response.getStatus()==SipServletResponse.SC_UNAUTHORIZED){
			newResponse.addHeader("WWW-Authenticate",response.getHeader("WWW-Authenticate"));
			register401.put(response.getFrom().getDisplayName(),response);
			logger.info("save authrization massage！！！！"+response.getFrom().getDisplayName());
		}*/

		logger.info("guocc::\n"+
		newResponse);
		if(response.getContent() != null) {
			newResponse.setContent(response.getContent(),response.getContentType());
		}
		newResponse.send();
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got Invite request:\n"
				+ request.toString());
		request.getSession().setAttribute("lastRequest",request);
		B2buaHelper helper=request.getB2buaHelper();
		SipServletRequest newRequest=helper.createRequest(request);
		helper.linkSipSessions(request.getSession(), newRequest.getSession());
		String user_Agent=request.getHeader("User-Agent");
		if(!user_Agent.contains("Asterisk")){
			String invite="a request form sipML5";
			//a request form sipML5
			String user=((SipURI)request.getTo().getURI()).getUser();
			SipURI asterisk=sipFactory.createSipURI(user,"10.206.16.36");
			asterisk.setPort(5060);
			newRequest.setRequestURI(asterisk);
			//Address asterisk2=sipFactory.createAddress("192.168.183.128");
			newRequest.pushRoute(asterisk);

		}else{
			//a request back to sipML5
			String user=((SipURI)request.getTo().getURI()).getUser();
			newRequest.setRequestURI(registeredUsersToIp.get(user).getURI());

		}
		if(request.getContent() != null) {
			newRequest.setContent(request.getContent(), request.getContentType());
		}
		newRequest.getSession().setAttribute("lastRequest",newRequest);
		newRequest.addHeader("User-Agent",request.getHeader("User-Agent"));
		newRequest.addHeader("Allow",request.getHeader("Allow"));
		//newRequest.addHeader("Supported",request.getHeader("Supported"));
		newRequest.send();
		//String fromUri = request.getFrom().getURI().toString();
		//logger.info(fromUri);
		
		//SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		//sipServletResponse.send();
	}
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got Ack request:\n"
				+ request.toString());
		B2buaHelper helper=request.getB2buaHelper();
		SipServletResponse response=(SipServletResponse)helper.getLinkedSession(request.getSession()).getAttribute("lastResponse");
		SipServletRequest newRequest=response.createAck();
		String user_Agent=request.getHeader("User-Agent");
		if(!user_Agent.contains("Asterisk")){
			//a ack from sipML5
			String user=request.getTo().getDisplayName();
			SipURI asterisk=sipFactory.createSipURI(user,"10.206.16.36");
			asterisk.setPort(5060);
			newRequest.setRequestURI(asterisk);
			//Address asterisk2=sipFactory.createAddress("192.168.183.128");
			//newRequest.pushRoute(asterisk);

		}else{
			//an Ack back to sipML5
			String ack="an Ack back to sipML5";
			String user=((SipURI)request.getTo().getURI()).getUser();
			newRequest.setRequestURI(registeredUsersToIp.get(user).getURI());
		}
		newRequest.addHeader("User-Agent",request.getHeader("User-Agent"));
		//newRequest.addHeader("Allow",request.getHeader("Allow"));
		//newRequest.addHeader("Supported",request.getHeader("Supported"));
		newRequest.send();
	}



	/*@Override
	protected void doOptions(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got Options request:\n"
				+ request.toString());
		B2buaHelper helper=request.getB2buaHelper();
		SipServletRequest newRequest;
		//HashMap<String,List<String>> map=new HashMap<String, List<String>>();
		//map.put("From",)
		newRequest=helper.createRequest(request);
		//newRequest.addHeader();


	}*/

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()){
			logger.info("Got BYE request:"+request);
		}
		B2buaHelper helper=request.getB2buaHelper();
		request.getSession().setAttribute("lastRequest",request);
		helper.getLinkedSession(request.getSession()).createRequest("BYE").send();
	}
}
