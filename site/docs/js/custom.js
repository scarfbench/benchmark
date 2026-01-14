// Custom JavaScript for SCARF site

// Citation functionality
document.addEventListener('DOMContentLoaded', function() {
    // Citation format switching
    const citationButtons = document.querySelectorAll('.citation-btn');
    const citationText = document.querySelector('.citation-text');
    const copyBtn = document.querySelector('.copy-btn');
    
    if (citationButtons.length > 0 && citationText) {
        citationButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const format = this.dataset.format;
                citationButtons.forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                
                // Update citation text based on format
                // This is a placeholder - replace with actual citation data
                const citations = {
                    bibtex: '@article{scarf2025,\n  title={SCARF: A Benchmark of Self-Contained Application Refactoring and Framework Migration Examples},\n  author={...},\n  year={2025}\n}',
                    apa: 'Author, A. (2025). SCARF: A Benchmark of Self-Contained Application Refactoring and Framework Migration Examples. Journal Name.',
                    mla: 'Author, A. "SCARF: A Benchmark of Self-Contained Application Refactoring and Framework Migration Examples." Journal Name, 2025.'
                };
                
                citationText.textContent = citations[format] || citations.bibtex;
            });
        });
        
        // Copy to clipboard
        if (copyBtn) {
            copyBtn.addEventListener('click', function() {
                const text = citationText.textContent;
                navigator.clipboard.writeText(text).then(() => {
                    const originalText = this.textContent;
                    this.textContent = 'Copied!';
                    setTimeout(() => {
                        this.textContent = originalText;
                    }, 2000);
                });
            });
        }
    }
    
    // Add copyright text to footer
    const footerMeta = document.querySelector('.md-footer-meta');
    if (footerMeta && !document.querySelector('.custom-copyright')) {
        const copyrightDiv = document.createElement('div');
        copyrightDiv.className = 'custom-copyright';
        copyrightDiv.style.cssText = 'text-align: right; width: 100%; padding-top: 0.5rem; color: var(--md-default-fg-color--light); font-size: 0.85rem; font-style: italic;';
        copyrightDiv.innerHTML = 'IBM © 2025. Made with ❤️ for the developer community from IBM Research.';
        footerMeta.appendChild(copyrightDiv);
    }
});
