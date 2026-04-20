package com.exo.web;

import com.exo.dao.PaperDAO;
import com.exo.dao.impl.PaperDAOImpl;
import com.exo.model.Paper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

@WebServlet("/paper/download")
public class DownloadPaperServlet extends HttpServlet {

    private final PaperDAO paperDao = new PaperDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing paper id");
            return;
        }

        try {
            int paperId = Integer.parseInt(idStr);
            Paper p = paperDao.findFileById(paperId);

            if (p == null || p.getFileData() == null || p.getFileData().length == 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF not found");
                return;
            }

            String fileName = (p.getFileName() == null || p.getFileName().isEmpty())
                    ? "paper-" + paperId + ".pdf"
                    : p.getFileName();

            String encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", " ");

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encoded + "\"");
            resp.setContentLength(p.getFileData().length);

            try (OutputStream out = resp.getOutputStream()) {
                out.write(p.getFileData());
            }

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
        } catch (Exception e) {
            throw new ServletException("Download failed", e);
        }
    }
}