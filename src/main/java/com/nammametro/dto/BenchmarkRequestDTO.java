package com.nammametro.dto;

public class BenchmarkRequestDTO {
    private int iterations = 50;
    private String sourceCode = "WFD";
    private String destinationCode = "MJC_P";

    public BenchmarkRequestDTO() {}

    public BenchmarkRequestDTO(int iterations, String sourceCode, String destinationCode) {
        this.iterations = iterations;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
    }

    public int getIterations() { return iterations; }
    public void setIterations(int iterations) { this.iterations = iterations; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }
}
