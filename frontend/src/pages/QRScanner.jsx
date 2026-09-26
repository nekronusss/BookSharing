import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Html5Qrcode } from 'html5-qrcode';
import { qrcodeAPI } from '../services/api';
import { FiArrowLeft } from 'react-icons/fi';

const QRScanner = () => {
  const navigate = useNavigate();
  const [scanning, setScanning] = useState(false);
  const [error, setError] = useState('');
  const [html5QrCode, setHtml5QrCode] = useState(null);

  useEffect(() => {
    return () => {
      // Cleanup: stop scanning when component unmounts
      if (html5QrCode) {
        html5QrCode.stop().catch(() => {});
      }
    };
  }, [html5QrCode]);

  const startScanning = async () => {
    try {
      setError('');
      setScanning(true);

      const qrCodeInstance = new Html5Qrcode('qr-reader');
      setHtml5QrCode(qrCodeInstance);

      await qrCodeInstance.start(
        { facingMode: 'environment' }, // Use back camera
        {
          fps: 10,
          qrbox: { width: 250, height: 250 },
        },
        async (decodedText) => {
          // QR code scanned successfully
          try {
            await qrCodeInstance.stop();
            setScanning(false);
            
            // Validate and fetch book by QR code
            const response = await qrcodeAPI.scan(decodedText);
            const book = response.data;
            
            // Navigate to book detail page
            navigate(`/books/${book.id}`);
          } catch (err) {
            setError(err.response?.data?.message || 'Book not found. Invalid QR code.');
            setScanning(false);
            await qrCodeInstance.stop();
          }
        },
        (errorMessage) => {
          // Ignore scanning errors (just keep scanning)
        }
      );
    } catch (err) {
      setError('Failed to start camera. Please check permissions.');
      setScanning(false);
      console.error('Error starting scanner:', err);
    }
  };

  const stopScanning = async () => {
    if (html5QrCode) {
      try {
        await html5QrCode.stop();
        setHtml5QrCode(null);
        setScanning(false);
        setError('');
      } catch (err) {
        console.error('Error stopping scanner:', err);
      }
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-6">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-4"
        >
          <FiArrowLeft className="mr-2" />
          Back
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Scan Book QR Code</h1>
        <p className="text-gray-600 mt-2">
          Point your camera at a book's QR code to view book details
        </p>
      </div>

      <div className="bg-white rounded-lg shadow-lg p-8">
        <div id="qr-reader" className="w-full max-w-md mx-auto mb-4"></div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <div className="flex justify-center space-x-4 mt-6">
          {!scanning ? (
            <button
              onClick={startScanning}
              className="px-6 py-3 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors"
            >
              Start Scanning
            </button>
          ) : (
            <button
              onClick={stopScanning}
              className="px-6 py-3 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
            >
              Stop Scanning
            </button>
          )}
        </div>

        <div className="mt-6 text-center text-sm text-gray-500">
          <p>Make sure to grant camera permissions when prompted</p>
        </div>
      </div>
    </div>
  );
};

export default QRScanner;

