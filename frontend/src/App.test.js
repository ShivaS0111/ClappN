import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Layout from './components/Layout/Layout';

// Simple test for Layout component without charts
test('renders layout with dashboard title', () => {
  render(
    <MemoryRouter>
      <Layout currentSection="dashboard" setCurrentSection={() => {}}>
        <div>Test Content</div>
      </Layout>
    </MemoryRouter>
  );
  const titleElement = screen.getByRole('heading', { name: /Dashboard/i });
  expect(titleElement).toBeInTheDocument();
});

test('renders layout with subtitle', () => {
  render(
    <MemoryRouter>
      <Layout currentSection="dashboard" setCurrentSection={() => {}}>
        <div>Test Content</div>
      </Layout>
    </MemoryRouter>
  );
  const subtitleElement = screen.getByText(/Welcome to your multi-vendor management platform/i);
  expect(subtitleElement).toBeInTheDocument();
});