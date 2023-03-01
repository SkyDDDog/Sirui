package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "VocCallbackVO", description = "腾讯云直播录制回调")
public class RecordCallbackVO {

    String appid;
    String app;
    String appname;
    String stream_id;
    String channel_id;
    String file_id;
    String record_file_id;
    String file_format;
    String task_id;
    String start_time;
    String end_time;
    String start_time_usec;
    String end_time_usec;
    String duration;
    String file_size;
    String stream_param;
    String video_url;
    String media_start_time;
    String record_bps;
    String callback_ext;


}
