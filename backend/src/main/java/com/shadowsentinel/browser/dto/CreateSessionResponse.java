package com.shadowsentinel.browser.dto;

public class CreateSessionResponse {

    private Long id;

    public CreateSessionResponse() {
    }

    public CreateSessionResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
