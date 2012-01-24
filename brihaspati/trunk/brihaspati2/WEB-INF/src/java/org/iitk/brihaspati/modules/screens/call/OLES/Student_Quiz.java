package org.iitk.brihaspati.modules.screens.call.OLES;

/*
 * @(#)Student_Quiz.java	
 *
 *  Copyright (c) 2010-2011 MHRD, DEI Agra. 
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
 *  
 *  Contributors: Members of MHRD, DEI Agra. 
 * 
 */

import java.util.Calendar;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.iitk.brihaspati.modules.screens.call.SecureScreen;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.turbine.om.security.User;
import org.iitk.brihaspati.modules.utils.TopicMetaDataXmlReader;
import org.iitk.brihaspati.modules.utils.ErrorDumpUtil;
import org.iitk.brihaspati.modules.utils.QuizFileEntry;
import org.iitk.brihaspati.modules.utils.QuizUtil;
import java.util.Vector;
import java.util.HashMap;
import java.io.File;
import java.util.StringTokenizer;
import org.iitk.brihaspati.modules.utils.MultilingualUtil;
import org.iitk.brihaspati.modules.utils.GroupUtil;
import org.iitk.brihaspati.modules.utils.ExpiryUtil;
import org.iitk.brihaspati.modules.utils.ListManagement;
import org.iitk.brihaspati.modules.utils.AdminProperties;
import org.iitk.brihaspati.modules.screens.call.SecureScreen;
import java.util.Date;
import org.apache.torque.util.Criteria;
import java.util.List;
import org.iitk.brihaspati.modules.utils.QuizMetaDataXmlReader;
import org.iitk.brihaspati.modules.utils.QuizMetaDataXmlWriter;
import org.iitk.brihaspati.modules.utils.XmlWriter;
import org.apache.turbine.services.servlet.TurbineServlet;
import org.iitk.brihaspati.modules.utils.UserGroupRoleUtil;
import org.iitk.brihaspati.modules.utils.GroupUtil;
import org.iitk.brihaspati.modules.utils.UserUtil;
import org.iitk.brihaspati.modules.utils.DbDetail;
import org.iitk.brihaspati.modules.utils.ListManagement;
import org.iitk.brihaspati.om.QuizPeer;
import org.iitk.brihaspati.om.Quiz;
import javax.servlet.http.*;
import org.iitk.brihaspati.modules.utils.CourseTimeUtil;
import org.iitk.brihaspati.modules.utils.ModuleTimeUtil;
/**
 *   This class contains code for quiz attempt part from student login
 *   @author  <a href="noopur.here@gmail.com">Nupur Dixit</a>
 */
public class Student_Quiz extends SecureScreen
{
	public void doBuildTemplate( RunData data, Context context )
	{
		ParameterParser pp=data.getParameters();
		String LangFile=data.getUser().getTemp("LangFile").toString();
		try{
			Attempt_Quiz.msg = 0;
			User user=data.getUser();
			String loginname=user.getName();
			String user_id=Integer.toString(UserUtil.getUID(loginname));
			String cid=(String)user.getTemp("course_id");
			Criteria crit=new Criteria();
			String Role=(String)user.getTemp("role");
			context.put("user_role",Role);
			String count = pp.getString("count","");	
			context.put("userID",user_id);
			if(count.isEmpty()){
				count=(String)user.getTemp("count");
			}			
			String type = pp.getString("type","");
			context.put("type",type);
			ErrorDumpUtil.ErrorLog("type is here"+type);
			if(type.equalsIgnoreCase("practice"))
				count="2";
			context.put("tdcolor",count);
			String filePath=TurbineServlet.getRealPath("/Courses"+"/"+cid+"/Exam/");
			String quizPath="/Quiz.xml";  
			String scorePath="/score.xml";
			File file=new File(filePath+"/"+quizPath);
			ErrorDumpUtil.ErrorLog("second to check");
			Vector quizList=new Vector();
			Vector attemptedQuizList=new Vector();
			Vector finalQuizList=new Vector();
			Vector futureQuizList = new Vector();
			QuizMetaDataXmlReader quizmetadata=null;
			if(type.equalsIgnoreCase("practice")){
				ErrorDumpUtil.ErrorLog("inside practice");
				if(!file.exists()){
					ErrorDumpUtil.ErrorLog("inside file not exist");
					data.setMessage(MultilingualUtil.ConvertedString("brih_nopracticequiz",LangFile));									
				}
				else{
					context.put("isFile","exist");
					quizmetadata=new QuizMetaDataXmlReader(filePath+"/"+quizPath);				
					quizList=quizmetadata.getPracticeQuiz_Detail();
					ErrorDumpUtil.ErrorLog("inside else "+quizList.size());
					if(quizList!=null){
						if(quizList.size()!=0){
							context.put("quizList",quizList);	              
						}
						else{
							data.setMessage(MultilingualUtil.ConvertedString("brih_nopracticequiz",LangFile));
						}
					}
					else
						data.setMessage(MultilingualUtil.ConvertedString("brih_nopracticequiz",LangFile));
				}	
			}
			else{
				if(!file.exists()){
					data.setMessage(MultilingualUtil.ConvertedString("brih_noquizannounced",LangFile));				
				}
				else{
					context.put("isFile","exist");
					quizmetadata=new QuizMetaDataXmlReader(filePath+"/"+quizPath);
					quizList=quizmetadata.readyToAttemptQuiz();
					
				//-------------------------------------------DEVENDRA-------------------------------------------------------
				//----------Functionality to get Security String for Login Student of a perticular Quiz from xml files------
					
					HashMap securityData=new HashMap(); 
					//String IPAddr=request.getRemoteAddr();
					String IPAddr=getClientIpAddr(data.getRequest());
					context.put("ip",IPAddr);
					ErrorDumpUtil.ErrorLog("IP Address is: "+IPAddr);
					
					if(quizList!=null && quizList.size()!=0){
						for(int i=0;i<quizList.size();i++){
							String quizid=((QuizFileEntry) quizList.elementAt(i)).getQuizID();
							String path=filePath+"/"+quizid;
							File secFile=new File(path+"/"+quizid+"_Security.xml");
							Vector collect=new Vector();
							if(secFile.exists()){
								ErrorDumpUtil.ErrorLog("inside if file exists :: "+path+"/"+quizid+"_Security.xml");
								QuizMetaDataXmlReader reader=new QuizMetaDataXmlReader(path+"/"+quizid+"_Security.xml");
								collect=reader.getSecurityDetail();
								if(collect!=null && collect.size()!=0){
									ErrorDumpUtil.ErrorLog("inside for collect not null "+collect.size());
									for(int j=0;j<collect.size();j++){
										String student=((QuizFileEntry) collect.elementAt(j)).getStudentID();
										if(student.equals(loginname)){
											ErrorDumpUtil.ErrorLog("inside for if student is "+student+" and login name is "+loginname);
											String securityString=((QuizFileEntry) collect.elementAt(j)).getSecurityID();
											String ip=((QuizFileEntry) collect.elementAt(j)).getIP();
											String temp=securityString+":"+ip;
											securityData.put(quizid, temp);
										}
									}
								}
							}
						}
					}
					context.put("securityData",securityData);
					ErrorDumpUtil.ErrorLog("inside Student_Quiz file user is: "+user+" and hash map is"+securityData.size()+" : "+securityData);
					
					//----------------------------------------------------END------------------------------------------------------------------
					
					
					futureQuizList = quizmetadata.listFutureQuiz();
					context.put("futureQuizList",futureQuizList);
					File scoreFile=new File(filePath+"/"+scorePath);
					ErrorDumpUtil.ErrorLog("Qiuz List for AttempQuiz"+quizList.size());
					if(quizList==null || quizList.size()==0){
						data.setMessage(MultilingualUtil.ConvertedString("brih_noquizattempt",LangFile));
						return;
					}
					if(!scoreFile.exists()){
						context.put("quizList",quizList);
					}
					else{
						quizmetadata=new QuizMetaDataXmlReader(filePath+"/"+scorePath);
						ErrorDumpUtil.ErrorLog("student user id is "+user_id);
						attemptedQuizList=quizmetadata.getFinalScore(user_id);
						String quizid,userid,quizid1;
						boolean found = false;
						ErrorDumpUtil.ErrorLog("The value of attempted quiz list is::"+attemptedQuizList.size()+"Quiz List ::"+quizList.size());
					if(quizList!=null && quizList.size()!=0){
						if(attemptedQuizList!=null && attemptedQuizList.size()!=0){
							for(int i=0;i<quizList.size();i++){
								for(int j=0;j<attemptedQuizList.size();j++){
									quizid = (((QuizFileEntry) quizList.elementAt(i)).getQuizID());
									quizid1 = (((QuizFileEntry) attemptedQuizList.elementAt(j)).getQuizID());
//									userid = (((QuizFileEntry) attemptedQuizList.elementAt(j)).getUserID());
									ErrorDumpUtil.ErrorLog("\n total quiz list :"+ i +" : "+quizid);
									ErrorDumpUtil.ErrorLog("\n attempted :"+j+" : "+quizid1 );									
									if(quizid.equalsIgnoreCase(quizid1)){
//										if(userid.equalsIgnoreCase(user_id)){
											found = true;
											break;
//										}
//										else{
//											found = false;
//											break;													
//										}
									}
									else
										found = false;
								}//end for
								ErrorDumpUtil.ErrorLog("\n found "+found);
								if(!found){
									QuizFileEntry q = (QuizFileEntry)quizList.get(i);
									finalQuizList.add(q);
									ErrorDumpUtil.ErrorLog("size of final quiz list ::"+finalQuizList);
								}
							}//end outer for
							if(finalQuizList!=null && finalQuizList.size()!=0){
								context.put("quizList",finalQuizList);
							}
							else
								data.setMessage(MultilingualUtil.ConvertedString("brih_noquizattempt",LangFile));
						}									
						else{
							//data.setMessage(MultilingualUtil.ConvertedString("brih_noquizannounced",LangFile));
							context.put("quizList",quizList);
						}
					}
					else
						data.setMessage(MultilingualUtil.ConvertedString("brih_noquizannounced",LangFile));
				}
				}
			}           
			/**
                         *Time calculaion for how long user use this page.
                         */
                         int userid=UserUtil.getUID(user.getName());
                         if((Role.equals("student")) || (Role.equals("instructor")))
                         {
                                CourseTimeUtil.getCalculation(userid);
                                ModuleTimeUtil.getModuleCalculation(userid);
                         }

		}catch(Exception e)
		{
			ErrorDumpUtil.ErrorLog("The exception in student_quiz ::"+e);
			data.setMessage(MultilingualUtil.ConvertedString("brih_exception"+e,LangFile));
		}
	}
	
	public static String getClientIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getRemoteAddr();
		}
		return ip;
		}
}

