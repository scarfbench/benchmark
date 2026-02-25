.PHONY: build clean install serve help venv

VENV = venv
PYTHON = $(VENV)/bin/python3
PIP = $(VENV)/bin/pip
MKDOCS = $(VENV)/bin/mkdocs

help:
	@echo "SCARF Website Build System (MkDocs)"
	@echo ""
	@echo "Available commands:"
	@echo "  make install    - Install Python dependencies in virtual environment"
	@echo "  make build      - Build the static site"
	@echo "  make serve      - Build and serve the site locally"
	@echo "  make clean      - Clean build artifacts"
	@echo ""

venv:
	@if [ ! -d "$(VENV)" ]; then \
		echo "Creating virtual environment..."; \
		python3 -m venv $(VENV); \
	fi

install: venv
	$(PIP) install -r requirements.txt

build: venv
	@if [ ! -f "$(MKDOCS)" ]; then $(MAKE) install; fi
	$(MKDOCS) build

serve: venv
	@if [ ! -f "$(MKDOCS)" ]; then $(MAKE) install; fi
	@echo "Starting MkDocs server at http://localhost:8000"
	$(MKDOCS) serve

clean:
	rm -rf site/
	rm -rf __pycache__/
	find . -type d -name "__pycache__" -exec rm -r {} + 2>/dev/null || true
	find . -type f -name "*.pyc" -delete 2>/dev/null || true

