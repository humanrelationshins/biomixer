package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.IsSvg;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * An SVG element with text surrounded by a rectangle.
 * 
 * @author elena
 * 
 */
public class SvgCircleExternalText implements IsSvg {
    private static final double THRESHOLD_TEXT_LENGTH = 150.0;

    public static final String DEFAULT_FONT_FAMILY = "Arial, sans-serif";

    public static final String DEFAULT_FONT_SIZE_PIXELS = "12px";

    public static final String DEFAULT_FONT_STYLE = "normal";

    public static final String DEFAULT_FONT_WEIGHT = "normal";

    public static final double TEXT_BUFFER_X = 5.0;

    public static final double TEXT_BUFFER_Y = 5.0;

    private SvgElement textElement;

    private SvgElement circleElement;

    private TextBoundsEstimator textBoundsEstimator;

    private SvgElementFactory svgElementFactory;

    private String fontFamily = DEFAULT_FONT_FAMILY;

    private String fontSize = DEFAULT_FONT_SIZE_PIXELS;

    private String fontStyle = DEFAULT_FONT_STYLE;

    private String fontWeight = DEFAULT_FONT_WEIGHT;

    private final String text;

    private int numberOfLines = 0;

    /*
     * Stores the tspan elements for multiple lines of text. Will be empty if
     * the text fits on one line and therefore doesn't need any tspans.
     */
    private Map<String, SvgElement> tspanElements = CollectionFactory
            .createStringMap();

    private String longestTextLine;

    private SvgElement container;

    public SvgCircleExternalText(String text,
            TextBoundsEstimator textBoundsEstimator,
            SvgElementFactory svgElementFactory, double radius) {
        container = svgElementFactory.createElement(Svg.SVG);
        container.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);
        this.textBoundsEstimator = textBoundsEstimator;
        this.svgElementFactory = svgElementFactory;
        this.text = text;
        createCircleWithText(radius);
    }

    @Override
    public SvgElement asSvgElement() {
        return container;
    }

    private void createCircleWithText(double radius) {

        circleElement = svgElementFactory.createElement(Svg.CIRCLE);
        initializeCircleElement(circleElement, radius);

        textElement = svgElementFactory.createElement(Svg.TEXT);
        setTextContent();
        initializeTextElementFontValues(textElement);

        centreTextElements();

        container.appendChild(circleElement);
        container.appendChild(textElement);
    }

    private void finishLine(StringBuilder currentLine, int textHeight,
            int currentLineWidth) {
        SvgElement tspan = svgElementFactory.createElement(Svg.TSPAN);

        String line = currentLine.toString().trim();
        tspan.setTextContent(line);

        tspan.setAttribute(Svg.X, TEXT_BUFFER_X);
        int numberOfPreviousTspanElements = tspanElements.size();
        int dy = numberOfLines == 0 ? 0 : textHeight
                * numberOfPreviousTspanElements;
        tspan.setAttribute(Svg.DY, dy);

        textElement.appendChild(tspan);
        tspanElements.put(line, tspan);

        if (currentLineWidth > getWidthOfLongestTextLine()) {
            longestTextLine = line;
        }

        numberOfLines++;
    }

    private double getCircleHeight() {
        return Double.parseDouble(circleElement.getAttributeAsString(Svg.R)) * 2;
    }

    private double getCircleWidth() {
        return Double.parseDouble(circleElement.getAttributeAsString(Svg.R)) * 2;
    }

    private double getCircleLeftX() {
        return Double.parseDouble(circleElement.getAttributeAsString(Svg.CX))
                - getCircleWidth();
    }

    private double getCircleTopY() {
        return Double.parseDouble(circleElement.getAttributeAsString(Svg.CY))
                - getCircleWidth();
    }

    private SizeInt getTextSize(String text) {
        try {
            textBoundsEstimator.setUp();
            textBoundsEstimator.configureFontStyle(fontStyle);
            textBoundsEstimator.configureFontWeight(fontWeight);
            textBoundsEstimator.configureFontSize(fontSize);
            textBoundsEstimator.configureFontFamily(fontFamily);
            return textBoundsEstimator.getSize(text);
        } finally {
            textBoundsEstimator.tearDown();
        }
    }

    public double getTotalHeight() {
        // TODO use BBox on container?
        return getCircleHeight();
    }

    public double getTotalWidth() {
        // TODO use BBox on container?
        return getCircleWidth();
    }

    private double getWidthOfLongestTextLine() {
        if (longestTextLine == null) {
            return 0.0;
        } else {
            return getTextSize(longestTextLine).getWidth();
        }
    }

    public void setBackgroundColor(String color) {
        circleElement.setAttribute(Svg.FILL, color);
    }

    public void setBorderColor(String color) {
        circleElement.setAttribute(Svg.STROKE, color);
    }

    private void centreTextElements() {
        if (numberOfLines == 1) {
            setTextElementXPosition(textElement);
        } else {
            for (Entry<String, SvgElement> entry : tspanElements.entrySet()) {
                setTextElementXPosition(entry.getValue());
            }
        }
    }

    private void setTextElementXPosition(SvgElement textElement) {
        textElement.setAttribute(Svg.X, getCircleLeftX() + 1.5
                * getCircleWidth() + TEXT_BUFFER_X);
        /*
         * the y-position of the text refers to the bottom of the FIRST LINE of
         * text
         */
        textElement.setAttribute(Svg.Y, TEXT_BUFFER_Y);
    }

    public void setCircleRadius(double radius) {
        circleElement.setAttribute(Svg.R, radius);
        centreTextElements();
    }

    // public void setCircleX(double x) {
    // circleElement.setAttribute(Svg.CX, x - radius);
    // }
    //
    // public void setCircleY(double y) {
    // circleElement.setAttribute(Svg.CY, y - radius);
    // }
    //
    // public void setY(double x) {
    // container.setAttribute(Svg.X, x);
    // }
    // public void setY(double y) {
    // container.setAttribute(Svg.Y, y);
    // }

    /**
     * Set default properties.
     * 
     * @param radius
     */
    private void initializeCircleElement(SvgElement circleElement, double radius) {
        circleElement.setAttribute(Svg.FILL, Colors.WHITE);
        circleElement.setAttribute(Svg.STROKE, Colors.BLACK);
        circleElement.setAttribute(Svg.CX, 0.0);
        circleElement.setAttribute(Svg.CY, 0.0);
        circleElement.setAttribute(Svg.R, radius);
    }

    private void initializeTextElementFontValues(SvgElement textElement) {
        textElement.setAttribute(Svg.FONT_FAMILY, DEFAULT_FONT_FAMILY);
        textElement.setAttribute(Svg.FONT_SIZE, DEFAULT_FONT_SIZE_PIXELS);
    }

    public void setEventListener(ChooselEventHandler listener) {
        container.setEventListener(listener);
    }

    public void setFontColor(String color) {
        textElement.setAttribute(Svg.FILL, color);
    }

    public void setFontWeight(String fontWeight) {
        double oldWidth = getWidthOfLongestTextLine();
        this.fontWeight = fontWeight;
        textElement.setAttribute(Svg.FONT_WEIGHT, fontWeight);
        double newWidth = getWidthOfLongestTextLine();
        // Circle doesn't need resizing around text, so no need for this.
        // updateCircleWidthAndPositionAroundText(newWidth - oldWidth);
    }

    // Circle doesn't need resizing around text, so no need for this.
    // private void updateCircleWidthAndPositionAroundText(double deltaWidth) {
    // setCircleX(getCircleLeftX() - (deltaWidth / 2));
    // setCircleWithText();
    // centreTextElements();
    // }

    private void setTextContent() {
        SizeInt textSize = getTextSize(text);
        if (textSize.getWidth() < THRESHOLD_TEXT_LENGTH) {
            textElement.setTextContent(text);
            longestTextLine = text;
            numberOfLines = 1;
        } else {
            // Need to wrap text
            String[] words = text.split(" ");

            int spaceWidth = getTextSize(" ").getWidth();
            int textHeight = textSize.getHeight();

            StringBuilder currentLine = new StringBuilder();
            int currentLineWidth = 0;
            for (String word : words) {
                int wordWidth = getTextSize(word).getWidth();
                if (currentLineWidth > 0) {
                    currentLine.append(" ");
                    currentLineWidth += spaceWidth;
                }

                if (currentLineWidth + wordWidth < THRESHOLD_TEXT_LENGTH) {
                    // the word can fit on current line
                    currentLine.append(word);
                    currentLineWidth += wordWidth;
                } else {
                    // end current line with previous word
                    finishLine(currentLine, textHeight, currentLineWidth);

                    // start new line with current word
                    currentLine = new StringBuilder();
                    currentLine.append(word);
                    currentLineWidth = wordWidth;
                }
            }

            // finish off the last line
            if (currentLineWidth > 0) {
                finishLine(currentLine, textHeight, currentLineWidth);
            }
        }
    }

}
