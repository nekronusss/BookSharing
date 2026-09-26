import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FiBook, FiSearch, FiUsers, FiHeart, FiArrowRight, FiShield, FiStar, FiCheck } from 'react-icons/fi';
import Button from '../components/ui/Button';

const Home = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="bg-white">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-b from-primary-50 to-white pt-16 pb-24 sm:pt-24 sm:pb-32">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="text-center">
            <Badge variant="primary" className="mb-4 px-4 py-1.5 uppercase tracking-widest text-xs font-bold">
              Community Driven Book Swap
            </Badge>
            <h1 className="text-5xl font-extrabold text-gray-900 tracking-tight sm:text-7xl mb-6">
              Share Your Books <br />
              <span className="text-primary-600">With the World</span>
            </h1>
            <p className="mt-6 max-w-2xl mx-auto text-lg text-gray-500 leading-8">
              Discover, swap, and connect with fellow book lovers. 
              Build your digital library, rate stories, and find your next great adventure 
              while giving your books a second life.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
              {user ? (
                <Button 
                  size="lg" 
                  className="px-10 py-4 text-lg shadow-xl shadow-primary-200"
                  onClick={() => navigate('/books')}
                >
                  Explore Catalog <FiArrowRight className="ml-2" />
                </Button>
              ) : (
                <>
                  <Button 
                    size="lg" 
                    className="px-10 py-4 text-lg shadow-xl shadow-primary-200"
                    onClick={() => navigate('/register')}
                  >
                    Get Started Free
                  </Button>
                  <Button 
                    variant="outline" 
                    size="lg" 
                    className="px-10 py-4 text-lg"
                    onClick={() => navigate('/books')}
                  >
                    Browse Catalog
                  </Button>
                </>
              )}
            </div>
            
            {/* Stats */}
            <div className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-8 border-t border-gray-100 pt-10">
              <div>
                <p className="text-3xl font-bold text-gray-900">5k+</p>
                <p className="text-sm text-gray-500">Books Shared</p>
              </div>
              <div>
                <p className="text-3xl font-bold text-gray-900">1.2k+</p>
                <p className="text-sm text-gray-500">Active Swappers</p>
              </div>
              <div>
                <p className="text-3xl font-bold text-gray-900">10k+</p>
                <p className="text-sm text-gray-500">Happy Readers</p>
              </div>
              <div>
                <p className="text-3xl font-bold text-gray-900">24h</p>
                <p className="text-sm text-gray-500">Avg. Swap Time</p>
              </div>
            </div>
          </div>
        </div>
        
        {/* Background elements */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full pointer-events-none z-0 overflow-hidden">
          <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary-100 rounded-full blur-[120px] opacity-50"></div>
          <div className="absolute bottom-[-10%] right-[-10%] w-[30%] h-[30%] bg-blue-100 rounded-full blur-[100px] opacity-40"></div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24 bg-white border-y border-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-base font-bold text-primary-600 uppercase tracking-widest mb-2">Features</h2>
            <p className="text-3xl font-extrabold text-gray-900 sm:text-4xl">
              Everything you need to manage swaps
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
            <div className="p-8 rounded-2xl border border-gray-100 hover:shadow-lg transition-shadow">
              <div className="h-12 w-12 rounded-xl bg-primary-100 flex items-center justify-center text-primary-600 mb-6">
                <FiBook className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Digital Library</h3>
              <p className="text-gray-500 leading-relaxed">
                Organize your collection with ease. Add tags, high-quality images, and 
                detailed descriptions to help others find your books.
              </p>
            </div>

            <div className="p-8 rounded-2xl border border-gray-100 hover:shadow-lg transition-shadow">
              <div className="h-12 w-12 rounded-xl bg-blue-100 flex items-center justify-center text-blue-600 mb-6">
                <FiShield className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Secure Swaps</h3>
              <p className="text-gray-500 leading-relaxed">
                Exchange with confidence. Our system uses digital identifiers and 
                verified statuses to ensure every swap is safe and transparent.
              </p>
            </div>

            <div className="p-8 rounded-2xl border border-gray-100 hover:shadow-lg transition-shadow">
              <div className="h-12 w-12 rounded-xl bg-purple-100 flex items-center justify-center text-purple-600 mb-6">
                <FiUsers className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Growing Community</h3>
              <p className="text-gray-500 leading-relaxed">
                Connect with local readers. Follow your favorite collectors and 
                get notified when they add new books to their shelf.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Social Proof Section */}
      <section className="py-24 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-2 gap-16 items-center">
            <div>
              <h2 className="text-3xl font-extrabold text-gray-900 mb-6">
                Trusted by thousands of bookworms worldwide.
              </h2>
              <p className="text-lg text-gray-500 mb-8">
                Join our community and start saving money while discovering 
                new worlds. Book sharing has never been this easy and rewarding.
              </p>
              <div className="space-y-4">
                {[
                  'Verified user profiles for safe exchanges',
                  'QR-code based swap confirmation',
                  'Interactive map for local meetups',
                  'Real-time notifications for requests'
                ].map((item, i) => (
                  <div key={i} className="flex items-center gap-3">
                    <div className="flex-shrink-0 h-5 w-5 bg-green-100 rounded-full flex items-center justify-center">
                      <FiCheck className="h-3 w-3 text-green-600" />
                    </div>
                    <span className="text-gray-700 font-medium">{item}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="relative">
              <div className="aspect-[4/3] bg-primary-100 rounded-2xl overflow-hidden shadow-2xl">
                 <img 
                   src="https://images.unsplash.com/photo-1521587760476-6c12a4b040da?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" 
                   alt="Bookshelf" 
                   className="w-full h-full object-cover mix-blend-multiply opacity-80"
                 />
              </div>
              <div className="absolute -bottom-6 -left-6 bg-white p-6 rounded-xl shadow-xl border border-gray-100 max-w-xs">
                 <div className="flex text-yellow-400 mb-2">
                   <FiStar className="fill-current" />
                   <FiStar className="fill-current" />
                   <FiStar className="fill-current" />
                   <FiStar className="fill-current" />
                   <FiStar className="fill-current" />
                 </div>
                 <p className="text-sm text-gray-700 italic">"I've found so many rare classics through this platform. The community is amazing!"</p>
                 <p className="text-xs font-bold text-gray-400 mt-3">— Sarah J., Member since 2023</p>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      {/* CTA Section */}
      <section className="py-20">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-primary-600 rounded-3xl p-12 text-center text-white shadow-2xl shadow-primary-200">
            <h2 className="text-3xl font-bold mb-6">Ready to clear your shelf?</h2>
            <p className="text-primary-100 mb-10 text-lg max-w-2xl mx-auto">
              Join our community today and give your books the next chapter they deserve. 
              It takes less than 2 minutes to get started.
            </p>
            <Button 
              variant="outline" 
              className="bg-white text-primary-600 border-none px-12 py-4 text-lg hover:bg-primary-50"
              onClick={() => navigate('/register')}
            >
              Sign Up Now
            </Button>
          </div>
        </div>
      </section>
    </div>
  );
};

const Badge = ({ children, variant, className }) => {
  const variants = {
    primary: 'bg-primary-100 text-primary-700',
  };
  return (
    <span className={`inline-block rounded-full ${variants[variant]} ${className}`}>
      {children}
    </span>
  );
}

export default Home;







