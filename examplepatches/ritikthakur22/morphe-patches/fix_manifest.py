import re

file_path = 'patches/src/main/kotlin/app/morphe/patches/shared/layout/branding/BaseCustomBrandingPatch.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('''val originalActivity = application.childNodes.toList()
                    .filterIsInstance<org.w3c.dom.Element>()
                    .firstOrNull {
                        it.tagName == "activity" && it.getAttribute("android:name") == activityAliasNameWithIntents
                    }''', 
'''val originalActivity = document.childNodes.findElementByAttributeValueOrThrow(
                    "android:name",
                    activityAliasNameWithIntents
                ) as? org.w3c.dom.Element''')

with open(file_path, 'w') as f:
    f.write(content)
