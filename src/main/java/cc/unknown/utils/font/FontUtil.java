package cc.unknown.utils.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.InputStream;

import cc.unknown.module.impl.visuals.HUD;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FontUtil {
    public FontRenderer montserrat;
    public FontRenderer roboto;
    private Font mont;
    private Font robo;

    private Font loadFont(String location, int size, int fontType) {
        Font font = null;

        try (InputStream is = HUD.class.getResourceAsStream("/assets/minecraft/haru/fonts/" + location)) {
            if (is == null) {
                throw new IllegalArgumentException("Font resource not found: " + location);
            }
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(fontType, size);
        } catch (FontFormatException | IllegalArgumentException e) {
            System.err.println("Error loading font: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }

        return font;
    }

    
    public void bootstrap() {
    	mont = loadFont("Montserrat-Bold.otf", 16, Font.PLAIN);
    	robo = loadFont("Roboto-Light.otf", 16, Font.PLAIN);
    	montserrat = new FontRenderer(mont, true, true);
    	roboto = new FontRenderer(robo, true, true);
    }
}