package nl.knaw.huygens.alexandria.dropwizard.resources;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(ResourcePaths.ABOUT)
@Path(ResourcePaths.ABOUT)
@Produces(MediaType.APPLICATION_JSON)
public class AboutResource {

  final AppInfo about;

  public AboutResource() {
    this(new AppInfo());
  }

  public AboutResource(final AppInfo appInfo) {
    this.about = appInfo;
  }

  @GET
  @Timed
  @ApiOperation(value = "Get some info about the server", response = AppInfo.class)
  public AppInfo getAbout() {
    return about;
  }
}
