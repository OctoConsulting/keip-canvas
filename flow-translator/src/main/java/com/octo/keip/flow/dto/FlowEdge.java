package com.octo.keip.flow.dto;

// TODO: Builder
public record FlowEdge(
    String id, String source, String target, String sourceHandle, String targetHandle) {}
