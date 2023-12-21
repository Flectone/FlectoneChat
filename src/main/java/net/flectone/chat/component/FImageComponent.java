package net.flectone.chat.component;

import lombok.Getter;
import net.flectone.chat.util.ColorUtil;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FImageComponent extends FComponent {

    private boolean isCorrect;

    private String text;

    public FImageComponent(@NotNull String imageUrl) {
        try {
            this.isCorrect = convertImageUrl(imageUrl);
        } catch (Exception ignored) {}
    }

    // Idea taken from here
    // https://github.com/QuiltServerTools/BlockBot/blob/5d5fa854002de2c12200edbe22f12382350ca7eb/src/main/kotlin/io/github/quiltservertools/blockbotdiscord/extensions/BlockBotApiExtension.kt#L136
    public boolean convertImageUrl(@NotNull String imageUrl) throws Exception {
        URL url = new URL(imageUrl);

        BufferedImage bufferedImage = ImageIO.read(url);
        if (bufferedImage == null) return false;

        text = FilenameUtils.getName(url.getPath());

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        if (height * width >= 8 * 1024 * 1024) return false;

        int stepSize = Math.max((int) Math.ceil(bufferedImage.getWidth() / 48.0), 1);
        int stepSquared = stepSize * stepSize;

        int x = 0;
        int y = 0;

        List<String> loreList = new ArrayList<>();
        loreList.add("");

        while (y < height) {
            StringBuilder text = new StringBuilder();
            while (x < width) {
                int rgb;

                if (stepSize != 1) {
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    for (int x2 = 0; x2 < stepSize; x2++) {
                        for (int y2 = 0; y2 < stepSize; y2++) {
                            int color = bufferedImage.getRGB(clamp(x + x2, 0, width - 1), clamp(y + y2, 0, height - 1));
                            r += (color >> 16) & 0xFF;
                            g += (color >> 8) & 0xFF;
                            b += color & 0xFF;
                        }
                    }

                    rgb = ((r / stepSquared) << 16) | ((g / stepSquared) << 8) | (b / stepSquared);
                } else {
                    rgb = bufferedImage.getRGB(x, y) & 0xFFFFFF;
                }

                String hexColor = String.format("#%06x", rgb);
                String pixel = "█";
                text.append(hexColor).append(pixel);
                x += stepSize;
            }

            loreList.add(text.toString());
            y += stepSize;
            x = 0;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String loreText : loreList) {
            String translatedColor = ColorUtil.translateHexToColor(loreText);
            stringBuilder.append(translatedColor).append("\n");
        }

        addHoverText(stringBuilder.toString());

        return true;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
