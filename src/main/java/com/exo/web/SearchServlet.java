package com.exo.web;

import com.exo.dao.SearchDAO;
import com.exo.dao.impl.SearchDAOImpl;
import com.exo.model.PlanetSummary;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private final SearchDAO dao = new SearchDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Read filters
        String q        = nz(req.getParameter("q"));
        Integer yFrom   = parseIntOrNull(req.getParameter("yFrom"));
        Integer yTo     = parseIntOrNull(req.getParameter("yTo"));
        String method   = nz(req.getParameter("method"));
        String facility = nz(req.getParameter("facility"));

        // Paging
        int size  = parseIntOrDefault(req.getParameter("size"), 20);
        int page  = Math.max(1, parseIntOrDefault(req.getParameter("page"), 1));
        int offset = (page - 1) * size;

        try {
            int total = dao.count(q, yFrom, yTo, method, facility);
            List<PlanetSummary> rows =
                    dao.search(q, yFrom, yTo, method, facility, size, offset);

            req.setAttribute("rows", rows);
            req.setAttribute("total", total);
            req.setAttribute("page", page);
            req.setAttribute("size", size);

            // -------- Compact, windowed pagination HTML --------
String ctx = req.getContextPath();
int totalPages = (int) Math.ceil(total / (double) size);
String encQ        = q == null ? "" : java.net.URLEncoder.encode(q, "UTF-8");
String encMethod   = method == null ? "" : java.net.URLEncoder.encode(method, "UTF-8");
String encFacility = facility == null ? "" : java.net.URLEncoder.encode(facility, "UTF-8");
String encFrom     = yFrom == null ? "" : String.valueOf(yFrom);
String encTo       = yTo   == null ? "" : String.valueOf(yTo);

// base query keeps current filters; page is appended later
String base = ctx + "/search?"
        + (encQ.isEmpty()        ? "" : "q=" + encQ + "&")
        + (encFrom.isEmpty()     ? "" : "yFrom=" + encFrom + "&")
        + (encTo.isEmpty()       ? "" : "yTo=" + encTo + "&")
        + (encMethod.isEmpty()   ? "" : "method=" + encMethod + "&")
        + (encFacility.isEmpty() ? "" : "facility=" + encFacility + "&")
        + "size=" + size + "&page=";

StringBuilder pag = new StringBuilder();
if (totalPages > 1) {
    // window size (e.g., show up to 7 links: current ±3)
    final int WINDOW = 7;
    int half = WINDOW / 2;
    int start = Math.max(1, page - half);
    int end   = Math.min(totalPages, start + WINDOW - 1);
    if (end - start + 1 < WINDOW) start = Math.max(1, end - WINDOW + 1);

    pag.append("<div class='pager' ")
       .append("style='display:flex;flex-wrap:wrap;gap:6px;align-items:center;")
       .append("justify-content:center;margin:10px 0;'>");

    // Prev
    if (page > 1) {
        pag.append("<a class='btn' href='").append(base).append(page - 1)
           .append("'>&laquo; Prev</a>");
    } else {
        pag.append("<span class='btn disabled'>&laquo; Prev</span>");
    }

    // First + ellipsis
    if (start > 1) {
        pag.append("<a class='btn' href='").append(base).append(1).append("'>1</a>");
        if (start > 2) pag.append("<span style='color:#c599ff'>…</span>");
    }

    // Windowed page numbers
    for (int p = start; p <= end; p++) {
        if (p == page) {
            pag.append("<span class='btn' style='background:#9b5de5'>").append(p).append("</span>");
        } else {
            pag.append("<a class='btn' href='").append(base).append(p).append("'>")
               .append(p).append("</a>");
        }
    }

    // Ellipsis + last
    if (end < totalPages) {
        if (end < totalPages - 1) pag.append("<span style='color:#c599ff'>…</span>");
        pag.append("<a class='btn' href='").append(base).append(totalPages).append("'>")
           .append(totalPages).append("</a>");
    }

    // Next
    if (page < totalPages) {
        pag.append("<a class='btn' href='").append(base).append(page + 1)
           .append("'>Next &raquo;</a>");
    } else {
        pag.append("<span class='btn disabled'>Next &raquo;</span>");
    }

    pag.append("</div>");
}
req.setAttribute("pagination", pag.toString());
            // --------------------------------------------------------

            req.getRequestDispatcher("/WEB-INF/views/search.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("error", "Search failed: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/search.jsp")
               .forward(req, resp);
        }
    }

    private String nz(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private Integer parseIntOrNull(String s) {
        try { return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    private int parseIntOrDefault(String s, int def) {
        try { return (s == null || s.trim().isEmpty()) ? def : Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }
}