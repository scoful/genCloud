package com.scoful.actions;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.scoful.utils.LoggerUtil;
import org.json.JSONObject;
import org.quartz.CronExpression;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

/**
 * @author: tangwei
 * @date: 2020/10/10 15:07
 */
public class MyTranslateAction extends AnAction {
    private long latestClickTime;

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!this.isFastClick(1000)) {
            LoggerUtil.init(getClass().getSimpleName(), LoggerUtil.DEBUG);
            //获取当前编辑器对象
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            //获取选择的数据模型
            if (editor != null) {
                SelectionModel selectionModel = editor.getSelectionModel();
                //获取当前选择的文本
                String selectedText = selectionModel.getSelectedText();
                if (StrUtil.isNotBlank(selectedText)) {
                    if (NumberUtil.isNumber(selectedText)) {
                        // 时间戳处理
                        if (selectedText.length() == 13) {
                            // 时间戳转换为LocalDateTime
                            LocalDateTime localDateTime = LocalDateTimeUtil.of(Long.parseLong(selectedText));
                            String format = LocalDateTimeUtil.format(localDateTime, DatePattern.NORM_DATETIME_PATTERN);
                            this.balloonNotice(format, editor);
                        } else {
                            this.balloonNotice(selectedText, editor);
                        }
                    } else if (selectedText.contains("==>  Preparing: ")) {
                        // myBatis打印的sql语句组装
                        if (selectedText.contains("\n")) {
                            String[] rows = selectedText.split("\n");
                            if (rows.length != 2) {
                                this.balloonNotice("只支持2行组装!", editor);
                                return;
                            }
                            String[] row1 = rows[0].split("==>  Preparing: ");
                            String[] preparings = row1[1].split("\\?");
                            List<String> preparingList = Arrays.asList(preparings);
                            String[] row2 = rows[1].split("==> Parameters: ");
                            ArrayList<String> newParametersList = new ArrayList<>();
                            if (row2.length > 1) {
                                String[] parameters = row2[1].split(",");
                                List<String> parametersList = Arrays.asList(parameters);
                                for (String s : parametersList) {
                                    String[] split = s.split("\\(");
                                    newParametersList.add(split[0]);
                                }
                            }
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i < preparingList.size(); i++) {
                                result.append(preparingList.get(i));
                                if (newParametersList.size() > 0 && i <= newParametersList.size() - 1) {
                                    result.append("'").append(newParametersList.get(i)).append("'");
                                }
                            }
                            this.balloonNotice(result.toString().replace("<", "&lt;").replace(">", "&gt;"), editor);
                            LoggerUtil.info(result.toString());
                        }
                    } else if (this.isCronExpression(selectedText)) {
                        // 选中cron表达式后显示下5次运行时间
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            CronExpression cronExpression = new CronExpression(selectedText);
                            Date currentTime = new Date();
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < 5; i++) {
                                Date nextTimePoint = cronExpression.getNextValidTimeAfter(currentTime);
                                stringBuilder.append(sdf.format(nextTimePoint)).append("\n");
                                currentTime = nextTimePoint;
                            }
                            this.balloonNotice(stringBuilder.toString(), editor);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } else {
                        this.balloonNotice(this.translateFunc(selectedText), editor);
                    }
                } else {
                    this.balloonNotice("请至少选中一点内容!", editor);
                }
            }
        }
    }

    /**
     * 判断是否cron表达式
     */
    private boolean isCronExpression(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 弹窗显示返回内容
     */
    private void balloonNotice(String selectedText, Editor editor) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        factory.createHtmlTextBalloonBuilder(selectedText, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null).setFadeoutTime(5000).createBalloon().show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
    }

    public static boolean isChinese(String str) {
        // 使用正则表达式匹配所有的中文字符
        return str.matches("[\\u4E00-\\u9FA5]+");
    }

    private boolean isEnglish(String str) {
        return str.matches("^[a-zA-Z]+$");
    }

    /**
     * 获取翻译结果
     */
    private String translateFunc(String txt) {
        String source = "en";
        String target = "zh";
        if (isChinese(txt)) {
            source = "zh";
            target = "en";
        } else if (isEnglish(txt)) {
            source = "en";
            target = "zh";
        }

        String url = "https://transmart.qq.com/api/imt";
        try {
            JSONObject data = new JSONObject();
            data.put("header", new JSONObject().put("fn", "auto_translation").put("session", "").put("client_key", "browser-chrome-117.0.0-Windows 10-4daf3e2e-b66e-43a1-944a-a8f6b42c9199-1696226243060").put("user", ""));
            data.put("type", "plain");
            data.put("model_category", "normal");
            data.put("text_domain", "general");
            data.put("source", new JSONObject().put("lang", source).put("text_list", new String[]{txt}));
            data.put("target", new JSONObject().put("lang", target));

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject result = new JSONObject(response.toString());
                if (Objects.equals(result.getJSONObject("header").getString("ret_code"), "succ")) {
                    LoggerUtil.info(result.getJSONArray("auto_translation").getString(0));
                    return result.getJSONArray("auto_translation").getString(0);
                } else {
                    return "--FAILED--!";
                }
            }
        } catch (Exception e) {
            LoggerUtil.error(e.toString());
            return "--ERROR,may be lost network--!";
        }
    }


    /**
     * 判断是否快速点击（1秒内）
     *
     * @param timeMillis
     * @return
     */
    private boolean isFastClick(long timeMillis) {
        long time = System.currentTimeMillis();
        long timeD = time - latestClickTime;
        if (0 < timeD && timeD < timeMillis) {
            return true;
        }
        latestClickTime = time;
        return false;
    }
}
