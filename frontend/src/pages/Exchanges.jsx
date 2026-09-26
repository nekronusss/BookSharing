import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { exchangeAPI } from '../services/api';
import { FiClock, FiCheck, FiX, FiAlertCircle, FiArrowRight, FiTruck, FiPackage } from 'react-icons/fi';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';

const Exchanges = () => {
  const [exchanges, setExchanges] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadExchanges();
  }, []);

  const loadExchanges = async () => {
    try {
      const response = await exchangeAPI.getAll();
      setExchanges(response.data);
    } catch (error) {
      console.error('Error loading exchanges:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: { variant: 'warning', icon: <FiClock className="mr-1" />, text: 'Pending' },
      WAITING_FOR_SHIPMENT: { variant: 'info', icon: <FiPackage className="mr-1" />, text: 'Waiting for Shipment' },
      IN_TRANSIT: { variant: 'primary', icon: <FiTruck className="mr-1" />, text: 'In Transit' },
      DELIVERED: { variant: 'success', icon: <FiCheck className="mr-1" />, text: 'Delivered' },
      COMPLETED: { variant: 'success', icon: <FiCheck className="mr-1" />, text: 'Completed' },
      REJECTED: { variant: 'danger', icon: <FiX className="mr-1" />, text: 'Rejected' },
      CANCELLED: { variant: 'gray', icon: <FiX className="mr-1" />, text: 'Cancelled' },
    };

    const config = statusConfig[status] || statusConfig.PENDING;
    return (
      <Badge variant={config.variant} className="flex items-center">
        {config.icon}
        {config.text}
      </Badge>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  const currentUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">My Exchanges</h1>
          <p className="text-gray-500 mt-1">Manage your book swap requests and status</p>
        </div>
        <Button
          onClick={() => navigate('/books')}
          className="md:w-auto"
        >
          Find Books to Swap
        </Button>
      </div>

      {exchanges.length === 0 ? (
        <div className="bg-white rounded-2xl border-2 border-dashed border-gray-200 py-16 text-center">
          <FiAlertCircle className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          <h3 className="text-lg font-bold text-gray-900 mb-2">No exchanges yet</h3>
          <p className="text-gray-500 mb-6 max-w-sm mx-auto">
            Start by creating an exchange request for a book you'd like to receive from the catalog.
          </p>
          <Button
            onClick={() => navigate('/books')}
            variant="primary"
          >
            Go to Catalog
          </Button>
        </div>
      ) : (
        <div className="grid gap-6">
          {exchanges.map((exchange) => (
            <Link
              key={exchange.id}
              to={`/exchanges/${exchange.id}`}
              className="group block bg-white rounded-xl shadow-sm border border-gray-100 hover:shadow-md hover:border-primary-100 transition-all p-6"
            >
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex-1">
                  <div className="flex flex-wrap items-center gap-2 mb-3">
                    {getStatusBadge(exchange.status)}
                    <span className="text-xs text-gray-400">
                      • {exchange.createdAt ? new Date(exchange.createdAt).toLocaleDateString() : 'Unknown date'}
                    </span>
                  </div>
                  
                  <div className="flex items-center gap-4 mb-4">
                    <div className="flex-1">
                      <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Requesting</p>
                      <h3 className="text-lg font-bold text-gray-900 group-hover:text-primary-600 transition-colors">
                        {exchange.requestedBookTitle}
                      </h3>
                    </div>
                    
                    <FiArrowRight className="text-gray-300 hidden md:block" />
                    
                    {exchange.offeredBookTitle && (
                      <div className="flex-1 text-right md:text-left">
                        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Offering</p>
                        <h3 className="text-lg font-bold text-gray-900">
                          {exchange.offeredBookTitle}
                        </h3>
                      </div>
                    )}
                  </div>
                  
                  <div className="flex items-center justify-between border-t border-gray-50 pt-4 mt-4">
                    <div className="flex items-center text-sm text-gray-600">
                      <div className="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-bold mr-2">
                        {(exchange.requesterUsername === currentUser?.username ? exchange.offererUsername : exchange.requesterUsername)?.charAt(0).toUpperCase()}
                      </div>
                      <span>
                        {exchange.requesterUsername === currentUser?.username
                          ? `With ${exchange.offererUsername} (Owner)`
                          : `From ${exchange.requesterUsername}`}
                      </span>
                    </div>
                    
                    <span className="text-primary-600 font-medium text-sm flex items-center group-hover:translate-x-1 transition-transform">
                      View details <FiArrowRight className="ml-1" />
                    </span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default Exchanges;

