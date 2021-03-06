/*
 * Copyright (c) 2016   The authors of WUIC
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.github.wuic.plugins.maven.test;

import com.github.wuic.engine.EngineService;
import com.github.wuic.nut.dao.NutDaoService;
import com.github.wuic.nut.filter.NutFilterService;
import com.github.wuic.plugins.maven.AnnotationToHtmlMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * Tests for the {@link com.github.wuic.plugins.maven.AnnotationToHtmlMojo}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.2
 */
@RunWith(JUnit4.class)
public class AnnoToAdocMojoTest {

    /**
     * Temporary.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Timeout.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(60);

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
        // Create MOJO
        final AnnotationToHtmlMojo mojo = new AnnotationToHtmlMojo();
        final File folder = temporaryFolder.newFolder();
        mojo.setOutput(folder.getAbsolutePath());

        // Invoke
        mojo.execute();

        // Assert
        Assert.assertTrue(new File(folder, EngineService.DEFAULT_SCAN_PACKAGE + ".html").exists());
        Assert.assertTrue(new File(folder, NutDaoService.DEFAULT_SCAN_PACKAGE + ".html").exists());
        Assert.assertTrue(new File(folder, NutFilterService.DEFAULT_SCAN_PACKAGE + ".html").exists());
   }
}