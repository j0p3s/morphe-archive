import re

file_path = 'patches/src/main/kotlin/app/morphe/patches/shared/layout/branding/BaseCustomBrandingPatch.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('''if (true) {
            // Copy user provided files
            val iconPathFile = File(customIcon!!.trim())''', 
'''if (useCustomIcon) {
            // Copy user provided files
            val iconPathFile = File(customIcon!!.trim())''')

with open(file_path, 'w') as f:
    f.write(content)
