/*
 * "Copyright (c) 2016   Capgemini Technology Services (hereinafter "Capgemini")
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * -   The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Any failure to comply with the above shall automatically terminate the license
 * and be construed as a breach of these Terms of Use causing significant harm to
 * Capgemini.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Capgemini shall not be used in
 * advertising or otherwise to promote the use or other dealings in this Software
 * without prior written authorization from Capgemini.
 *
 * These Terms of Use are subject to French law.
 *
 * IMPORTANT NOTICE: The WUIC software implements software components governed by
 * open source software licenses (BSD and Apache) of which CAPGEMINI is not the
 * author or the editor. The rights granted on the said software components are
 * governed by the specific terms and conditions specified by Apache 2.0 and BSD
 * licenses."
 */


package com.github.wuic.plugins.maven.test;

import com.github.wuic.plugins.maven.StaticHelperMojo;
import com.github.wuic.test.TestHelper;
import com.github.wuic.util.IOUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Tests for the {@link StaticHelperMojoTest}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.1
 */
@RunWith(JUnit4.class)
public class StaticHelperMojoTest {

    /**
     * <p>
     * Default test.
     * </p>
     *
     * @throws MojoExecutionException if test fails
     * @throws IOException if test fails
     */
    @Test
    public void defaultTest() throws MojoExecutionException, IOException {
        final String wuicXml = IOUtils.normalizePathSeparator(getClass().getResource("/wuic.xml").toString());
        final String wuicProperties = IOUtils.normalizePathSeparator(getClass().getResource("/wuic.properties").toString());
        final String currentDir = IOUtils.normalizePathSeparator(new File(".").toURI().toURL().toString());

        // Create MOJO
        final StaticHelperMojo mojo = new StaticHelperMojo();
        mojo.setRelocateTransformedXml(Boolean.TRUE);
        mojo.setXml(wuicXml.substring(currentDir.length() - 2));
        mojo.setProperties(wuicProperties.substring(currentDir.length() - 2));
        mojo.setOutput("generated");
        mojo.setContextPath("/");

        // Mock
        final AtomicReference<String> resources = new AtomicReference<String>();
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        final Build build = Mockito.mock(Build.class);
        final File out = new File(System.getProperty("java.io.tmpdir"), "wuic-static-test");
        Mockito.when(build.getDirectory()).thenReturn(out.getAbsolutePath());
        Mockito.when(build.getOutputDirectory()).thenReturn(out.getAbsolutePath());
        Mockito.when(mavenProject.getBuild()).thenReturn(build);
        Mockito.when(mavenProject.getBasedir()).thenReturn(new File("."));
        mojo.setMavenProject(mavenProject);
        final MavenProjectHelper helper = Mockito.mock(MavenProjectHelper.class);
        mojo.setProjectHelper(helper);
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                resources.set(invocationOnMock.getArguments()[1].toString());
                return null;
            }
        }).when(helper).addResource(Mockito.any(MavenProject.class), Mockito.anyString(), Mockito.any(List.class), Mockito.any(List.class));

        // Invoke
        mojo.execute();

        // Verify
        final File parent = new File(System.getProperty("java.io.tmpdir"), "wuic-static-test/generated/");
        Assert.assertTrue(new File(parent, "js").listFiles()[0].list()[0].equals("aggregate.js"));

        Boolean found = Boolean.FALSE;
        File[] files = new File(parent, "css").listFiles();

        for (int i = 0; i < files.length && !found; found = files[i++].list()[0].equals("aggregate.css"));
        Assert.assertTrue(found);

        InputStream is;
        files = new File(parent, "html").listFiles();
        File html = null;
        for (int i = 0; i < files.length && html == null; html = files[i++].listFiles()[0].getName().endsWith(".html") ? files[i - 1].listFiles()[0] : null);
        Assert.assertNotNull(html);

        // Read html content
        is = new FileInputStream(html);
        final String htmlStr = IOUtils.readString(new InputStreamReader(is));
        is.close();

        // Extract URL (workflow and nut name)
        final int start = htmlStr.indexOf("href=\"") + 7;
        final int end = htmlStr.indexOf("\"", start);
        final String url = htmlStr.substring(start, end);
        final int endId = url.indexOf('/');
        final String id = url.substring(0, endId);
        final String nutPath = url.substring(endId + 1);

        // Read metadata content
        is = new FileInputStream(new File(resources.get(), "wuic-static/" + id));
        final String wuicStatic = IOUtils.readString(new InputStreamReader(is));
        is.close();

        // Assert nut is known in metadata
        Assert.assertTrue(nutPath + " not in:\n" + wuicStatic, wuicStatic.contains(nutPath));

        // Assert resources are closed
        TestHelper.delete(out);
    }
}