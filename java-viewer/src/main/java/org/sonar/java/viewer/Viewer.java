/*
 * SonarQube Java
 * Copyright (C) 2012-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.viewer;

import org.apache.log4j.BasicConfigurator;
import org.sonar.java.ast.ASTViewer;
import org.sonar.java.cfg.CFG;
import org.sonar.java.cfg.CFGDebug;
import org.sonar.java.cfg.CFGViewer;
import org.sonar.java.se.EGViewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

public class Viewer {

  private static final String DEFAULT_SOURCE_CODE = "/public/example/example.java";

  public static void main(String[] args) {
    BasicConfigurator.configure();

    exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
    staticFiles.location("/public");
    port(9999);

    get("/", (req, res) -> renderIndex(req));
  }

  private static String renderIndex(Request req) {
    HashMap<String, String> model = new HashMap<>();
    String defaultFileContent = defaultFileContent();

    CFG cfg = CFGViewer.buildCFG(defaultFileContent);

    model.put("sampleCode", defaultFileContent);
    model.put("cfg", CFGDebug.toString(cfg));

    model.put("dotAST", ASTViewer.toDot(defaultFileContent));
    model.put("dotCFG", CFGViewer.toDot(cfg));
    model.put("dotEG", EGViewer.toDot(defaultFileContent, cfg.blocks().get(0).id()));

    return new VelocityTemplateEngine().render(new ModelAndView(model, "velocity/index.vm"));
  }

  private static String defaultFileContent() {
    String result;
    try {
      Path path = Paths.get(Viewer.class.getResource(DEFAULT_SOURCE_CODE).toURI());
      result = new String(Files.readAllBytes(path));
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      result = "// Unable to read default file:\\n\\n";
    }
    return result;
  }

}
