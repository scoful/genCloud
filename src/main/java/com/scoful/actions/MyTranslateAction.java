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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: tangwei
 * @date: 2020/10/10 15:07
 */
public class MyTranslateAction extends AnAction {
    private long latestClickTime;

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!isFastClick(1000)) {
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
                            balloonNotice(format, editor);
                        } else {
                            balloonNotice(selectedText, editor);
                        }
                    } else if (selectedText.contains("==>  Preparing: ")) {
                        // myBatis打印的sql语句组装
                        if (selectedText.contains("\n")) {
                            String[] rows = selectedText.split("\n");
                            if (rows.length != 2) {
                                balloonNotice("只支持2行组装!", editor);
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
                            balloonNotice(result.toString(), editor);
                            LoggerUtil.info(result.toString());
                        }
                    } else {
                        balloonNotice(getTransLatesResult(selectedText), editor);
                    }
                } else {
                    balloonNotice("请至少选中一点内容!", editor);
                }
            }
        }
    }

    private void balloonNotice(String selectedText, Editor editor) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        factory.createHtmlTextBalloonBuilder(selectedText, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
               .setFadeoutTime(5000)
               .createBalloon()
               .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
    }

    private String getTransLatesResult(String txt) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> forEntity = restTemplate.getForEntity("https://fanyi.youdao.com/openapi.do?keyfrom=lulua-net&key=620584095&type=data&doctype=json&version=1.1&q={txt}", Map.class, txt);
            HttpStatus statusCode = forEntity.getStatusCode();
            if (statusCode.is2xxSuccessful()) {
                List data = (List) forEntity.getBody().get("translation");
                Object result = data.get(0);
                LoggerUtil.info(forEntity.getBody().toString());
                return result.toString();
            }
        } catch (Exception e) {
            LoggerUtil.error(e.toString());
            return "--ERROR,may be lost network--!";
        }
        return "--FAILED--!";
    }

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
