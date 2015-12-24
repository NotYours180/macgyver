/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.plugin.cmdb;

import io.macgyver.core.web.UIContext;
import io.macgyver.core.web.UIContextDecorator;

public class CmdbMenuDecorator implements UIContextDecorator {

	public CmdbMenuDecorator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void call(UIContext ctx) {
		ctx.getOrCreateMenuItem("inventory").label("Inventory").style("fa fa-list");
		ctx.getOrCreateMenuItem("inventory","app-instances").label("App Instances").url("/plugin/cmdb/app-instances");
	}

}