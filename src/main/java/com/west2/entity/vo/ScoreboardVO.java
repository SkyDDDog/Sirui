package com.west2.entity.vo;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class ScoreboardVO {
    private String id;
    private String projectName;
    private String teamA;
    private String teamB;
    private String scoreA;
    private String scoreB;
    private String colorA;
    private String colorB;
    private String gameName;
    private String time;
    private Boolean stopFlag;

    private String course;
    private Boolean showed;

    private Integer aCuts;
    private Integer bCuts;

    protected static Map<Integer, String> map = Maps.newHashMap();

    public static String getCourse(Integer no) {
        if (no==null) {
            return null;
        }
        return (String) map.get(no);
    }

    static {
        map.put(1, "1st");
        map.put(2, "2nd");
        map.put(3, "3rd");
        map.put(4, "fourth");
        map.put(5, "OT");
        map.put(6, "OT2");
    }

}
