package it.project.uploadContentToSocialNetworks.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CommandEnum {
    START("/start"),
    CREATE_REQUEST("/createrequest"),
    CREATE_REQUEST_ALTER("/createrequest2"),
    STOP_REQUEST("/stoprequest"),
    HELP("/help"),
    PAY("/pay"),
    GET_MY_ID("/getmyid");

    private final String command;

    public static CommandEnum of(String command) {
        return Arrays.stream(values())
                .filter(target -> target.getCommand().equalsIgnoreCase(command))
                .findAny()
                .orElse(null);
    }
}

