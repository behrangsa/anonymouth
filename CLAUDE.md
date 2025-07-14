# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Running the Application

**Main Entry Point**: `edu.drexel.psal.anonymouth.gooie.ThePresident`
- To run Anonymouth: Execute the `main()` method in `ThePresident.java`
- **Development Environment**: Maven-compatible (Eclipse, IntelliJ, VS Code)
- **Java Version**: Requires Java 7+ (Java 8 and Maven 3.3.9 configured via `.sdkmanrc`)
- **Required Directory**: `jsan_resources/` must be in the running directory

## Build System

**Maven-based build system** with standard project structure:

### Build Commands
- **Compile**: `mvn compile`
- **Package**: `mvn package` (creates JAR in `target/`)
- **Clean**: `mvn clean`
- **Run**: `java -jar target/anonymouth-0.5.0-jar-with-dependencies.jar`
- **Format Code**: `mvn spotless:apply` (applies Eclipse formatter for Java 8)
- **Check Formatting**: `mvn spotless:check` (validates code formatting)

### Maven Structure
- **Source**: `src/main/java/`
- **Resources**: `src/main/resources/`
- **Tests**: `src/test/java/` (directory created but no tests present)
- **Dependencies**: Managed via `pom.xml`

### Dependencies
- Most dependencies resolved from Maven Central
- Some legacy JARs use system scope (fasttag, jgaap, jaws-bin, ui)
- `jsan_resources/` directory included as build resource

## Architecture Overview

### Core Package Structure

- **`edu.drexel.psal.anonymouth.engine`** - Document processing and analysis logic
  - Key classes: `DocumentProcessor`, `DataAnalyzer`, `HighlighterEngine`
  - Handles stylometric analysis and feature extraction

- **`edu.drexel.psal.anonymouth.gooie`** - GUI components and drivers
  - Key classes: `GUIMain` (central hub), `ThePresident` (entry point), `EditorDriver`
  - Follows Panel/Window + Driver class pattern for UI components

- **`edu.drexel.psal.anonymouth.utils`** - Data structures for document processing
  - Key classes: `TaggedDocument`, `DocumentParser`, `Word`, `Sentence`
  - Core data structures for text analysis

- **`edu.drexel.psal.anonymouth.helpers`** - General utilities
  - Key classes: `FileHelper`, `ImageLoader`, `ErrorHandler`

- **`edu.drexel.psal.jstylo`** - Stylometric analysis library (separate component)
  - Provides authorship detection and feature extraction capabilities
  - Contains analyzers, event drivers, and canonicizers

### Application Flow

1. **Launch**: `ThePresident` initializes splash screen and `GUIMain`
2. **Setup**: `StartWindow` displays for configuration
3. **Processing**: `DocumentProcessor` handles document analysis
4. **Main GUI**: `GUIMain` serves as central hub for all functionality

## Dependencies

Located in `/lib/` directory:
- **Weka 3.7.9** - Machine learning and data mining
- **Stanford POS Tagger** - Part-of-speech tagging
- **JFreeChart 1.0.14** - Data visualization
- **Microsoft Translator API 0.6.1** - Translation services
- **JGAAP 5.2.0** - Authorship attribution analysis

## Resources and Configuration

- **`jsan_resources/`** - Critical runtime resources
  - **Corpora**: AMT, Drexel, and Enron datasets for training
  - **Feature sets**: XML configuration files for stylometric features
  - **POS models**: Stanford tagger models and language resources
- **Properties**: Managed through `PropertiesUtil` class

## Known Issues

- **Threading problems** with Stanford POS tagger causing memory exceptions (especially on macOS)
- **No formal testing** infrastructure or unit tests
- **GUI threading** runs on initial thread (violates Swing best practices)
- **Legacy codebase** from 2011 with some outdated dependencies

## Development Notes

- **Naming Convention**: UI components split into `[Class]Panel/Window` and `[Class]Driver` classes
- **Instance Management**: `GUIMain` serves as central instance hub for nearly all classes
- **Mavenized project**: Now includes standard Maven structure and build system
- **Code Formatting**: Spotless plugin configured with Eclipse formatter 4.7.3a (Java 8 compatible)
- **No modern dev infrastructure**: No CI/CD, code quality tools, or automated testing
- **Academic Research Tool**: Mature codebase but lacks modern development practices