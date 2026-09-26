import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { exchangeAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  FiClock,
  FiCheck,
  FiX,
  FiArrowLeft,
  FiAlertCircle,
  FiTruck,
  FiPackage,
  FiMessageSquare,
  FiInfo,
  FiRefreshCw,
} from 'react-icons/fi';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import StatusStepper from '../components/ui/StatusStepper';
import ConfirmModal from '../components/ui/ConfirmModal';

const ExchangeDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [exchange, setExchange] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [trackingNumber, setTrackingNumber] = useState('');
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, type: null });

  useEffect(() => {
    loadExchange();
  }, [id]);

  const loadExchange = async () => {
    try {
      const response = await exchangeAPI.getById(id);
      setExchange(response.data);
    } catch (error) {
      console.error('Error loading exchange:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (type) => {
    setConfirmModal({ isOpen: true, type });
  };

  const executeAction = async () => {
    const type = confirmModal.type;
    setProcessing(true);
    try {
      if (type === 'accept') await exchangeAPI.accept(id);
      if (type === 'reject') await exchangeAPI.reject(id);
      if (type === 'cancel') await exchangeAPI.cancel(id);
      if (type === 'ship') await exchangeAPI.markAsShipped(id, trackingNumber || null);
      if (type === 'confirm') await exchangeAPI.confirmReceipt(id);
      
      setConfirmModal({ isOpen: false, type: null });
      loadExchange();
    } catch (error) {
      alert(error.formattedMessage || 'Error performing action');
    } finally {
      setProcessing(false);
    }
  };

  const getSteps = (status) => {
    const steps = [
      { label: 'Requested', status: 'completed' },
      { label: 'Accepted', status: 'upcoming' },
      { label: 'Shipping', status: 'upcoming' },
      { label: 'Delivered', status: 'upcoming' },
      { label: 'Completed', status: 'upcoming' },
    ];

    if (status === 'REJECTED' || status === 'CANCELLED') {
      return [{ label: status.charAt(0) + status.slice(1).toLowerCase(), status: 'error' }];
    }

    const statusMap = {
      PENDING: 0,
      ACCEPTED: 1,
      WAITING_FOR_SHIPMENT: 1,
      IN_TRANSIT: 2,
      DELIVERED: 3,
      COMPLETED: 4,
    };

    const currentIdx = statusMap[status] || 0;
    
    return steps.map((step, idx) => {
      if (idx < currentIdx) return { ...step, status: 'completed' };
      if (idx === currentIdx) return { ...step, status: 'current' };
      return step;
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!exchange) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <FiAlertCircle className="mx-auto h-12 w-12 text-gray-400 mb-4" />
        <h2 className="text-xl font-bold text-gray-900">Exchange not found</h2>
        <Button variant="outline" onClick={() => navigate('/exchanges')} className="mt-4">
          Back to My Exchanges
        </Button>
      </div>
    );
  }

  const isRequester = user && exchange.requesterUsername === user.username;
  const isOfferer = user && exchange.offererUsername === user.username;

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Button
        variant="ghost"
        onClick={() => navigate('/exchanges')}
        className="mb-6 -ml-2"
      >
        <FiArrowLeft className="mr-2" />
        Back to My Exchanges
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Details */}
        <div className="lg:col-span-2 space-y-8">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-8 border-b border-gray-50">
              <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold text-gray-900">Exchange Details</h1>
                <Badge 
                  variant={
                    exchange.status === 'COMPLETED' ? 'success' : 
                    exchange.status === 'REJECTED' || exchange.status === 'CANCELLED' ? 'danger' : 
                    'warning'
                  }
                >
                  {exchange.status.replace(/_/g, ' ')}
                </Badge>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8 relative">
                <div className="space-y-4">
                  <p className="text-xs font-bold text-gray-400 uppercase tracking-widest">Requester (You)</p>
                  <div className="p-4 bg-primary-50 rounded-xl border border-primary-100">
                    <h3 className="text-lg font-bold text-primary-900 mb-1">{exchange.requestedBookTitle}</h3>
                    <p className="text-sm text-primary-700">From: {exchange.offererUsername}</p>
                  </div>
                </div>

                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 hidden md:block">
                  <div className="bg-white p-2 rounded-full border border-gray-100 shadow-sm">
                    <FiRefreshCw className="h-5 w-5 text-gray-400" />
                  </div>
                </div>

                <div className="space-y-4">
                  <p className="text-xs font-bold text-gray-400 uppercase tracking-widest text-right md:text-left">Offerer</p>
                  <div className="p-4 bg-gray-50 rounded-xl border border-gray-200">
                    <h3 className="text-lg font-bold text-gray-900 mb-1">
                      {exchange.offeredBookTitle || 'No book offered'}
                    </h3>
                    <p className="text-sm text-gray-600">To: {exchange.requesterUsername}</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="p-8 space-y-8">
              {exchange.message && (
                <div>
                  <h3 className="text-sm font-bold text-gray-900 mb-3 flex items-center">
                    <FiMessageSquare className="mr-2 text-gray-400" />
                    Message from requester
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-xl text-gray-700 italic border-l-4 border-primary-200">
                    "{exchange.message}"
                  </div>
                </div>
              )}

              <div className="border-t border-gray-50 pt-8">
                <h3 className="text-sm font-bold text-gray-900 mb-6">Progress</h3>
                <StatusStepper steps={getSteps(exchange.status)} />
              </div>
            </div>
          </div>

          {/* Action Cards */}
          {(exchange.status === 'PENDING' || (exchange.canConfirm && exchange.status !== 'COMPLETED')) && (
            <div className="bg-primary-900 rounded-2xl p-8 text-white shadow-xl shadow-primary-100">
              <div className="flex items-start gap-4 mb-6">
                <div className="bg-primary-800 p-3 rounded-lg">
                  <FiInfo className="h-6 w-6 text-primary-300" />
                </div>
                <div>
                  <h3 className="text-xl font-bold">Action Required</h3>
                  <p className="text-primary-200 mt-1">
                    {exchange.status === 'PENDING' 
                      ? (isOfferer ? 'Please review this request.' : 'Waiting for owner response.')
                      : 'Please confirm that you have received the books.'}
                  </p>
                </div>
              </div>
              
              <div className="flex flex-wrap gap-4">
                {exchange.canAccept && (
                  <Button variant="primary" className="bg-white text-primary-900 hover:bg-primary-50 border-none px-8" onClick={() => handleAction('accept')}>
                    Accept Request
                  </Button>
                )}
                {exchange.canReject && (
                  <Button variant="danger" className="bg-red-500 hover:bg-red-600 border-none px-8" onClick={() => handleAction('reject')}>
                    Reject
                  </Button>
                )}
                {exchange.canCancel && (
                  <Button variant="ghost" className="text-primary-100 hover:bg-primary-800 hover:text-white" onClick={() => handleAction('cancel')}>
                    Cancel Request
                  </Button>
                )}
                {exchange.canConfirm && (
                  <Button variant="primary" className="bg-white text-primary-900 hover:bg-primary-50 border-none px-8 w-full md:w-auto" onClick={() => handleAction('confirm')}>
                    Confirm Receipt
                  </Button>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Info & Sidebar */}
        <div className="space-y-8">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 className="text-sm font-bold text-gray-900 mb-4 uppercase tracking-wider">Information</h3>
            <div className="space-y-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Requested on</span>
                <span className="font-medium text-gray-900">
                  {new Date(exchange.createdAt).toLocaleDateString()}
                </span>
              </div>
              {exchange.acceptedAt && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Accepted on</span>
                  <span className="font-medium text-gray-900">
                    {new Date(exchange.acceptedAt).toLocaleDateString()}
                  </span>
                </div>
              )}
              <div className="flex justify-between text-sm border-t border-gray-50 pt-4">
                <span className="text-gray-500">Exchange ID</span>
                <span className="font-mono text-xs text-gray-400">#{exchange.id}</span>
              </div>
            </div>
          </div>

          {exchange.trackingNumber && (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="text-sm font-bold text-gray-900 mb-4 uppercase tracking-wider">Tracking</h3>
              <div className="p-3 bg-gray-50 rounded-lg font-mono text-center text-primary-700">
                {exchange.trackingNumber}
              </div>
              <p className="text-xs text-gray-500 mt-2 text-center">
                Use this number to track your package on the carrier's website.
              </p>
            </div>
          )}

          <div className="bg-gray-50 rounded-2xl p-6 border border-gray-100">
            <h3 className="text-sm font-bold text-gray-900 mb-3">Help & Safety</h3>
            <ul className="text-xs text-gray-500 space-y-2">
              <li>• Only meet in public, well-lit places.</li>
              <li>• Inspect the book condition before confirming.</li>
              <li>• If shipping, use tracked delivery for safety.</li>
              <li>• Problems? Contact support.</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Modals */}
      <ConfirmModal
        isOpen={confirmModal.isOpen}
        onClose={() => setConfirmModal({ isOpen: false, type: null })}
        onConfirm={executeAction}
        loading={processing}
        title={
          confirmModal.type === 'accept' ? 'Accept Exchange' :
          confirmModal.type === 'reject' ? 'Reject Exchange' :
          confirmModal.type === 'cancel' ? 'Cancel Exchange' :
          confirmModal.type === 'confirm' ? 'Confirm Receipt' : 'Confirm Action'
        }
        message={
          confirmModal.type === 'accept' ? 'Are you sure you want to accept this request? You will be committed to the exchange.' :
          confirmModal.type === 'reject' ? 'Are you sure you want to reject this request? This action cannot be undone.' :
          confirmModal.type === 'cancel' ? 'Are you sure you want to cancel your request?' :
          confirmModal.type === 'confirm' ? 'By confirming receipt, you acknowledge that you have received the books in good condition. The exchange will be marked as completed.' :
          'Are you sure you want to perform this action?'
        }
        variant={confirmModal.type === 'reject' || confirmModal.type === 'cancel' ? 'danger' : 'primary'}
      />
    </div>
  );
};

export default ExchangeDetail;

