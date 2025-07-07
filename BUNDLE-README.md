# Vibe Sandbox Bundle Creation

This directory contains a script to create clean, distribution-ready git bundles of the Vibe Sandbox project.

## Quick Usage

```bash
./create-clean-bundle.sh
```

This will create/replace `vibe-sandbox.bundle` in the current directory.

## What the Script Does

1. **Removes existing bundle** - Deletes any existing `vibe-sandbox.bundle`
2. **Creates clean repository** - Sets up a temporary git repository
3. **Copies source files** - Excludes problematic files like:
   - `node_modules/`
   - `*.bundle` files
   - `.git/` directory
   - `build/`, `dist/` directories
   - Log files and OS files
4. **Creates fresh commit** - Single clean commit with timestamp
5. **Generates bundle** - Creates the final `vibe-sandbox.bundle`
6. **Verifies bundle** - Ensures the bundle is valid
7. **Cleans up** - Removes temporary files

## Bundle Details

- **Size**: ~77KB (vs 85MB with problematic history)
- **Contents**: Complete source code and configuration
- **Recipients need**: `npm install` to get dependencies

## For Recipients

Share these instructions with people receiving the bundle:

```bash
# Clone the bundle
git clone vibe-sandbox.bundle vibe-sandbox

# Install dependencies
cd vibe-sandbox
npm install

# Start development server
npm start
```

## Troubleshooting

If the script fails:

1. **Permission denied**: Run `chmod +x create-clean-bundle.sh`
2. **Git errors**: Ensure you're in a git repository
3. **Space issues**: Check available disk space (script needs ~200MB temporarily)

## Script Location

The script should be run from the project root directory where `package.json` is located.
