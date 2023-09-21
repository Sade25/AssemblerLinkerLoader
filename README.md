# CSE Machine Assembler, Linker, and Simulator

A comprehensive system for assembling, linking, loading, and simulating code written for the CSE machine.

## Table of Contents

- [Description](#description)
- [Features](#features)

## Description

This project provides an integrated system that takes in assembly code for the CSE machine, assembles it into an intermediate representation, links multiple such representations, loads them into a simulated machine's memory, and then interprets and executes the instructions.

## Features

- **Assembler**: Transforms assembly code into intermediate representation (object files).
- **Linking Loader**: Links multiple object files, resolves addresses, and outputs an absolute executable file.
- **Simulator**: Loads and interprets the instructions on the simulated CSE machine.
  - **Modes**:
    - **Quiet Mode**: Executes without interruption.
    - **Trace Mode**: Provides a trace of execution.
    - **Step Mode**: Allows for step-by-step execution with user prompts.

