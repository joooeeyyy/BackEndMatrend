package com.tekbridge.alertapp.Firebase;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MediaDisplay {
    private Long videoId;
    private String videoUrl;
    private List<String> pictures;
    private boolean statusPending;
    private boolean isUploading;
    private String userId;
    private String businessName;

    public Map<String, Object> toMap() {
        return Map.of(
                "videoId", videoId,
                "videoUrl", videoUrl,
                "pictures", pictures,
                "statusPending", statusPending,
                "isUploading", isUploading,
                "userId", userId,
                "businessName", businessName
        );
    }

    public static MediaDisplay fromMap(Map<String, Object> map) {
        return MediaDisplay.builder()
                .videoId((Long) map.get("videoId"))
                .videoUrl((String) map.get("videoUrl"))
                .pictures((List<String>) map.get("pictures"))
                .statusPending((boolean) map.get("statusPending"))
                .isUploading((boolean) map.get("isUploading"))
                .userId((String) map.get("userId"))
                .businessName((String) map.get("businessName"))
                .build();
    }
}