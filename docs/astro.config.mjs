import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: 'https://scarfbench.github.io/',
  base: '/benchmark/',
  integrations: [
    starlight({
      title: '🧣 ScarfBench',
      description:
        'A Benchmark of Self-Contained Application Refactoring and Framework Migration Examples',
      social: [
        { icon: 'github', label: 'ScarfBench on GitHub', href: 'https://github.com/scarfbench/benchmark' }
      ],
      sidebar: [
        {
          label: 'ScarfBench',
          items: [
            { label: 'Home', slug: 'index' },
            { label: 'Setup', slug: 'installing' },
            { label: 'Quickstart', slug: 'quickstart' },
            { label: 'Submit Solution', slug: 'submit' },
          ]
        },
        { label: 'Leaderboard', slug: 'leaderboard' },
        {
          label: 'Benchmark',
          items: [
            { label: 'Overview', slug: 'benchmark' },
            {
              label: 'Focused Examples',
              items: [
                { label: 'Business Domain', slug: 'business_domain' },
                { label: 'Dependency Injection', slug: 'dependency_injection' },
                { label: 'Infrastructure', slug: 'infrastructure' },
                { label: 'Integration', slug: 'integration' },
                { label: 'Persistence', slug: 'persistence' },
                { label: 'Presentation', slug: 'presentation' },
                { label: 'Security', slug: 'security' }
              ]
            },
            {
              label: 'Whole Applications',
              items: [
                { label: 'Overview', slug: 'whole_applications' },
                { label: 'CargoTracker', slug: 'cargotracker' },
                { label: 'Coffee Shop', slug: 'coffee_shop' },
                { label: 'DayTrader', slug: 'daytrader' },
                { label: 'PetClinic', slug: 'petclinic' },
                { label: 'RealWorld', slug: 'realworld' }
              ]
            }
          ]
        },
        {
          label: 'Resources',
          items: [
            { label: 'Resources', slug: 'resources' },
            { label: 'Citation', slug: 'citation' }
          ]
        },
      ]
    })
  ]
});
