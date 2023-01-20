package thymeleaf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Template {
    BufferedReader templateReader;
    StringBuilder html;

    String ifOrUnlessValue = null;

    public Template(String template) {
        try {
            this.templateReader = new BufferedReader(new FileReader(template));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void render(TemplateContext ctx, PrintWriter out) throws Exception {
        html = new StringBuilder();

        String line = templateReader.readLine();
        while (line != null) {
            if (line.contains("t:text")) {
                setText(line, ctx);
            } else if (line.contains("t:each")) {
                setForEach(line, ctx);
            } else {
                html.append(line).append("\n");
            }

            line = templateReader.readLine();
        }
        saveToFile(out, html.toString());
    }

    private void setIf(String line, Object o, Class<?> cl) throws Exception {
        int start = line.indexOf(".") + 1;
        int end = line.indexOf("}");
        String fieldName = line.substring(start, end);
        Field f = cl.getDeclaredField(fieldName);

        start = line.indexOf("==") + 4;
        end = line.indexOf("'\"");
        Object searchValue = line.substring(start, end);
        Object fieldValue = String.valueOf(f.get(o));

        start = line.indexOf("t:text=\"") + 8;
        end = line.indexOf("\"", start + 1);
        String res = line.substring(start, end);

        if (searchValue.equals(fieldValue)) {
            ifOrUnlessValue = res;
            html.append(res);
        }
    }

    private void setUnless(String line, Object o, Class<?> cl) throws Exception {
        int start = line.indexOf(".") + 1;
        int end = line.indexOf("}");
        String fieldName = line.substring(start, end);
        Field f = cl.getDeclaredField(fieldName);

        start = line.indexOf("==") + 4;
        end = line.indexOf("'\"");
        Object searchValue = line.substring(start, end);
        Object fieldValue = String.valueOf(f.get(o));

        start = line.indexOf("t:text=\"") + 8;
        end = line.indexOf("\"", start + 1);
        String res = line.substring(start, end);

        if (!searchValue.equals(fieldValue)) {
            html.append(res);
        }
    }

    private void setForEach(String line, TemplateContext ctx) throws Exception {
        int start = line.indexOf("t:each");
        String tr = line.substring(0, start - 1) + ">";

        String searchKey = handelLine(line, start);

        Object[] classes = ctx.classes.get(searchKey);

        List<String> lines = new ArrayList<>();
        line = templateReader.readLine();
        while (!line.contains("</tr>")) {
            lines.add(line);
            line = templateReader.readLine();
        }

        for (Object o : classes) {
            html.append(tr).append("\n");
            Class<?> cl = o.getClass();

            for (String l : lines) {
                if (l.contains("t:if")) {
                    setIf(l, o, cl);
                    continue;
                } else if (l.contains("t:unless")) {
                    if (ifOrUnlessValue == null) {
                        setUnless(l, o, cl);
                        continue;
                    }

                    ifOrUnlessValue = null;
                    continue;
                }

                if (l.contains("<td>") || l.contains("</td>")) {
                    html.append(l.contains("</td>") ? l + "\n" : l);
                    continue;
                }

                String fieldName = setText(l, ctx);
                Field f = cl.getDeclaredField(fieldName);
                html.append(f.get(o)).append("</td>").append("\n");
            }
            html.append("</tr>").append("\n");
        }
    }

    private static String handelLine(String line, int start) {
        start += 8;
        int end = line.indexOf(":", start);
        //String key = line.substring(start, end);

        start = line.indexOf("{");
        end = line.indexOf("}\"");

        return line.substring(start + 1, end);
    }

    private String setText(String line, TemplateContext ctx) {
        int start = line.indexOf("t:text");
        html.append(line.substring(0, start)).append(">");

        start += 8;
        int end = line.indexOf("}\"");
        String placeholder = line.substring(start, end);

        if (placeholder.contains("#")) {
            String[] values = placeholder.substring(2).split("\\.");
            String res = ctx.welcomeMessages.get(values[0]);
            html.append(res).append("</span>");
        }

        String[] values = placeholder.substring(2).split("\\.");
        return values[1];
    }

    private static void saveToFile(PrintWriter out, String html) {
        Document doc = Jsoup.parse(html);
        out.print(doc);
        out.flush();
        out.close();
    }
}
