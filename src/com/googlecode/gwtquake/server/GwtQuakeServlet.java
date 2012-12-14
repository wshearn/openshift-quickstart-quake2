/*
Copyright (C) 2010 Copyright 2010 Google Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.googlecode.gwtquake.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GwtQuakeServlet extends HttpServlet {


    @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    File f = new File("war/GwtQuake.html");
    DataInputStream in = new DataInputStream(new FileInputStream(f));
    byte[] bytes = new byte[in.available()];
    in.readFully(bytes);
    in.close();

    String html = new String(bytes, "UTF-8");
    html = html.replace(
        "<!--SERVER_ADDRESS_PLACEHOLDER-->",
        "<script>var __serverAddress = '" + InetAddress.getLocalHost().getHostAddress() + "';</script>");
    rsp.getWriter().print(html);
    rsp.flushBuffer();
    rsp.setStatus(200);
  }

}
