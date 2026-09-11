package dev.gokhun.convert;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "junit")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dev.gokhun.convert")
@IncludeEngines("cucumber")
@SelectClasspathResource("dev/gokhun/convert")
@Suite
final class RunConvertTest {}
