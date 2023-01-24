package thymeleaf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Template {
    Document template;

    private static Document resultHtml = null;

    private static TemplateContext context;

    public Template(String template) {
        try {
            this.template = Jsoup.parse(new File(template));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void render(TemplateContext ctx, PrintWriter out) throws Exception {
        resultHtml = template.clone();
        context = ctx;
        r();

        out.print(resultHtml);
        out.close();
    }

    private static void r() throws Exception {
        Elements ifElements = resultHtml.getElementsByAttribute("t:if");
        for (Element e : ifElements) {
            setIf(e);
        }

        Elements eachElements = resultHtml.getElementsByAttribute("t:each");
        for (Element e : eachElements) {
            List<?> list = getList(e);

            Element parentElement = e.parent();
            Elements children = e.children();
            Tag tag = e.tag();
            e.remove();

            setEach(list, parentElement, children, tag);
        }

        Elements textElements = resultHtml.getElementsByAttribute("t:text");
        for (Element e : textElements) {
            if (e.attr("t:text").contains("#")) {
                String text = e.attr("t:text");
                String res = getResultText(text, null);

                e.removeAttr("t:text");
                e.append(res);
            }
        }
    }

    private static void setIf(Element e) throws Exception {
        String text = e.attr("t:if");
        String res = getResultText(text, null);

        //example: ${student.gender} == 'm'
        int start = text.indexOf("== ");
        String ifCondition = text.substring(start + 4, text.length() - 1);

        if (res.equals(ifCondition)) {
            setIfOrUnlessResult(e, "t:if");
            return;
        }
        if (!e.nextElementSibling().hasAttr("t:unless")) {
            e.remove();
            return;
        }

        Element unlessElement = e.nextElementSibling();
        String textUnless = unlessElement.attr("t:unless");
        String resUnless = getResultText(textUnless, null);

        //example: ${student.gender} == 'm'
        start = textUnless.indexOf("== ");
        String ifConditionUnless = textUnless.substring(start + 4, text.length() - 1);

        if (!resUnless.equals(ifConditionUnless)) {
            setIfOrUnlessResult(unlessElement, "t:unless");
            e.remove();
            return;
        }
        unlessElement.remove();
        e.remove();
    }

    private static void setIfOrUnlessResult(Element element, String attributeKey) {
        String resText = element.attr("t:text");
        element.append(resText);
        element.removeAttr("t:text");
        element.removeAttr(attributeKey);
    }

    private static void setEach(List<?> list, Element parentElement, Elements children, Tag elementTag) throws Exception {
        for (Object classObject : list) {
            Element newElement = new Element(elementTag.toString());
            parentElement.appendChild(newElement);

            for (Element child : children) {
                Tag tag = child.tag();
                Element newChildElement = new Element(tag.toString());
                newElement.appendChild(newChildElement);
                r();

                String text = child.attr("t:text");
                String res = getResultText(text, classObject);

                newChildElement.append(res);
            }
        }
    }

    private static List<?> getList(Element e) {
        String text = e.attr("t:each");

        //example: student: ${students}
        int start = text.indexOf("${");
        String key = text.substring(start + 2, text.length() - 1);

        Object ob = context.classes.get(key);

        if (!ob.getClass().isArray())
            throw new IllegalArgumentException("Each uses array");

        return Arrays.asList((Object[]) ob);
    }

    private static String getResultText(String text, Object o) throws Exception {
        int dotIndex = text.indexOf(".");
        int end = text.indexOf("}");

        //example: #{welcome.message}
        String key = text.substring(2, dotIndex);
        String fieldName = text.substring(dotIndex + 1, end);

        if (o != null) {
            Field f = o.getClass().getField(fieldName);
            return String.valueOf(f.get(o));
        }

        Object cl = context.classes.get(key);
        Field f = cl.getClass().getField(fieldName);

        return String.valueOf(f.get(cl));
    }
}
