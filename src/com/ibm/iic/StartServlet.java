package com.ibm.iic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cloudant.OperatorDB;
import com.ibm.json.java.JSONObject;

/**
 * Servlet implementation class StartServlet
 */
@WebServlet("/StartServlet")
public class StartServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor. Establishes a connection to the service, and sets up
	 * the request and the reply queues
	 * 
	 * @return
	 * 
	 * @throws Exception
	 * @see HttpServlet#HttpServlet()
	 */
	public StartServlet() throws Exception {
		super();
		Worker wk = Worker.getInstance();
		if (wk.isUnlock()) {
			try {
				new Thread(wk).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			wk.setUnlock(false);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		OperatorDB odb = new OperatorDB();
		Map<String, String> map = odb.getRequestMap();
		JSONObject jo = new JSONObject();
		jo.putAll(map);
		try {
			PrintWriter out = response.getWriter();
			out.println(jo.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
