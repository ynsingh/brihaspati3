package org.iitk.brihaspati.modules.screens.call.TA_Management;

/*
 * @(#)InstAdminTA_Management.java 
 *
 *  Copyright (c) 2013 ETRG,IIT Kanpur.
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
 *  Contributors: Members of ETRG, I.I.T. Kanpur
 *
 */



import java.util.List;
import java.util.Vector;
import org.apache.turbine.util.RunData;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.security.torque.om.TurbineUserPeer;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.commons.lang.StringUtils;
import org.iitk.brihaspati.modules.screens.call.SecureScreen_Institute_Admin;
import org.iitk.brihaspati.modules.utils.GroupUtil;
import org.iitk.brihaspati.modules.utils.UserUtil;
import org.iitk.brihaspati.modules.utils.UserGroupRoleUtil;
import org.iitk.brihaspati.modules.utils.ListManagement;
import org.iitk.brihaspati.modules.utils.ErrorDumpUtil;
import org.iitk.brihaspati.modules.utils.InstituteDetailsManagement;
import org.iitk.brihaspati.modules.utils.CourseUtil;
import org.iitk.brihaspati.modules.utils.CourseManagement;
/**
 *   This class contains code for disply all TA and Instructor for institute
 *   @author  <a href="richa.tandon1@gmail.com">Richa Tandon</a>
*/

public class InstAdminTA_Management extends SecureScreen_Institute_Admin {

        public void doBuildTemplate( RunData data, org.apache.velocity.context.Context context ){
	
		try {	
			User user = data.getUser();	
			ParameterParser pp=data.getParameters();
			String instituteId=(data.getUser().getTemp("Institute_id")).toString();
			//Get List of registered course for institute
			Vector courseList=InstituteDetailsManagement.getInstituteCourseDetails(instituteId);
			context.put("courseList",courseList);			
			String cname=pp.getString("cname","");
                        context.put("cname",cname);
			if(StringUtils.isNotBlank(cname)){
				int gid=GroupUtil.getGID(cname);
				String coursename=CourseUtil.getCourseName(cname);
                	        context.put("coursename",coursename);
				/**
 				 * Get list of registered TA and instructor in vector
 				 */
				Vector TA_List=UserGroupRoleUtil.getUID(gid,8);
				Vector userList1=new Vector();
				Vector Instr_List=UserGroupRoleUtil.getUID(gid,2);
				Vector userList2=new Vector();
				List users=null;
				List usersList=null;
				
				/**
 				*Get user detail from database and context put to vm for display 
 				*/ 
				for(int i=0;i<TA_List.size();i++)
	                        { 
	                                int uid=Integer.parseInt(TA_List.elementAt(i).toString());
	                                Criteria crit=new Criteria();
	                                crit.add(TurbineUserPeer.USER_ID,uid);
	                                try{
	                                        users=TurbineUserPeer.doSelect(crit);
	                                }
        	                        catch(Exception e){data.setMessage("The error in InstAdminTA_Management !!"+e);}
	                                userList1.addElement(users);
        	                }
				context.put("userdetail",userList1);
				for(int j=0;j<Instr_List.size();j++)
	                        { 
	                                int uid=Integer.parseInt(Instr_List.elementAt(j).toString());
					String Username=UserUtil.getLoginName(uid);
					boolean check_Primary=CourseManagement.IsPrimaryInstructor(cname,Username);
					if(check_Primary==true)
					{
						context.put("PrimaryInstr",Username);		
					}
					Criteria crit=new Criteria();
	                                crit.add(TurbineUserPeer.USER_ID,uid);
	                                try{
	                                        usersList=TurbineUserPeer.doSelect(crit);
	                                }
        	                        catch(Exception e){data.setMessage("The error in InstAdminTA_Management !!"+e);}
	                                userList2.addElement(usersList);
        	                }
				context.put("Instrdetail",userList2);
			}
	 }catch(Exception e){
                        data.setMessage("The exception in InstAdminTA_Management :- "+e);}
	}
}
