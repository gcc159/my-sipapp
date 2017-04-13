package com.mycompany.app.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * Created by 郭骋城 on 2017/4/12.
 */
public class Servlet extends HttpServlet {
    static String Password="3426883";
    static String Caller="";
    static String Callee="";

    final static HashMap<String,HashSet<String>> callSet=new HashMap<String,HashSet<String>> ();
    final static LinkedList<String> unreadyList=new LinkedList<String>();
    ScheduledExecutorService scheduledThreadPool=Executors.newScheduledThreadPool(100);

    String callee;
    String statusCode;
    String reason;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Hello World!</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Hello World!</h1>");
        out.println("</body>");
        out.println("</html>");
        System.out.println("this is the NEW Servlets");
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException,ServletException
    {
        JSONObject json=new JSONObject();
        //JSONArray jsonArray = new JSONArray();

        response.setContentType("application/x-json");
        PrintWriter pw=response.getWriter();
        request.setCharacterEncoding("utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String method=request.getParameter("Method");

        System.out.println(method);
        //System.out.println(request.toString());
        if(method.equals("SET-NEW-CALL")){
            System.out.println("here!!!");
            System.out.println(request.toString());
            String pass=request.getParameter("password");

            String caller=request.getParameter("caller");
            String callee=request.getParameter("callee");
            System.out.println(pass+caller+callee);
            doNewCall(pass,caller,callee);
            json.put("statusCode",statusCode);
            json.put("reason",reason);

        }else if(method.equals("HAS-MY-CALL")){
            String caller=request.getParameter("caller");
            doHasCall(caller);
            response.setStatus(HttpServletResponse.SC_OK);
            json.put("caller",caller);
            json.put("callee",callee);
            json.put("statusCode",statusCode);
            json.put("reason",reason);

        }else if(method.equals("TIMING-NEW-CALL")){
            String caller=request.getParameter("caller");
            String callee=request.getParameter("callee");
            int second=0;
            try {
                second=Integer.parseInt(request.getParameter("second"));

            }catch (NumberFormatException e){
                e.printStackTrace();
            }
            unreadyList.addLast(caller+':'+callee);
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    timeTask();
                }
            };
            scheduledThreadPool.schedule(runnable,second,TimeUnit.SECONDS);
            json.put("statusCode","SUCCESS");
            json.put("reason","You have submit a new Three-Way Calling");

        }
        pw.print(json.toString());
        System.out.println(json.toString());
        pw.flush();
        pw.close();
    }
    public boolean doNewCall(String pass,String caller,String callee) {
        if (pass.equals(Password)) {
            Caller = caller;
            Callee = callee;
            HashSet<String> calleeSet = callSet.get(caller);
            if (calleeSet.contains(callee)) {
                statusCode = "ERROR";
                reason = "Your Calling is already exist";
            } else {
                calleeSet.add(callee);
                statusCode = "SUCCESS";
                reason = "Has submit the new three Party Calling";
            }
            return true;
        } else {
            statusCode = "ERROR";
            reason = "Wrong Password";
            return false;
        }
    }
    public boolean doHasCall(String caller)
    {
        HashSet<String> calleeSet = callSet.get(caller);
        if(calleeSet==null) {
            calleeSet = new HashSet<String>();
            callSet.put(caller, calleeSet);
        }

        if(!calleeSet.isEmpty()) {
            Iterator<String> newcallee=calleeSet.iterator();
            callee=newcallee.next();
            calleeSet.remove(callee);
            statusCode="SUCCESS";
            reason="You have a new Three-Party Calling";
            return true;
        }
        else {
            callee="";
            statusCode="NULL";
            reason="You haven't new Three-Party Calling.";
            return false;
        }
    }
    public void timeTask(){
        System.out.println("触发时间任务！！！");
        String[] target=unreadyList.getFirst().split(":");
        String caller=target[0];
        String callee=target[1];
        HashSet<String> calleeSet = callSet.get(caller);
        if(calleeSet==null){
            calleeSet=new HashSet<String>();
            callSet.put(caller,calleeSet);
        }
        if (!calleeSet.contains(callee))
            calleeSet.add(callee);

    }
}
