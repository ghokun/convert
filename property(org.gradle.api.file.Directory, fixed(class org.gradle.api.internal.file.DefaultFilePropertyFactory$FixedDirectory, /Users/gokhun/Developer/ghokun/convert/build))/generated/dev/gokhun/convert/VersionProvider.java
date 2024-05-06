
package dev.gokhun.convert;

final class VersionProvider implements picocli.CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        return new String[] {"0.0.0-SNAPSHOT"};
    }
}
